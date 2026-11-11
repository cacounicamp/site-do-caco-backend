package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.AnswerInputDTO;
import com.caco.sitedocaco.features.forms.dto.request.SubmitFormRequestDTO;
import com.caco.sitedocaco.features.forms.dto.response.AnswerDTO;
import com.caco.sitedocaco.features.forms.dto.response.MySubmissionDTO;
import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormAnswer;
import com.caco.sitedocaco.features.forms.entity.FormAnswerOption;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.FormStatus;
import com.caco.sitedocaco.features.forms.entity.FormSubmission;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.repository.FormAnswerRepository;
import com.caco.sitedocaco.features.forms.repository.FormOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.FormRepository;
import com.caco.sitedocaco.features.forms.repository.FormSubmissionRepository;
import com.caco.sitedocaco.features.users.entity.User;
import com.caco.sitedocaco.features.users.service.UserService;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.shared.storage.FileStorage;
import com.caco.sitedocaco.shared.storage.StoredFile;
import com.caco.sitedocaco.shared.storage.UploadRequest;
import com.caco.sitedocaco.shared.storage.kind.DocumentKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormSubmissionService {

    private static final int FILE_NAME_MAX_LENGTH = 255;

    private final FormRepository formRepository;
    private final FormQuestionRepository questionRepository;
    private final FormOptionRepository optionRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormAnswerRepository answerRepository;
    private final UserService userService;
    private final FileStorage fileStorage;

    // ── modo de resposta única ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MySubmissionDTO getMySubmission(String slug) {
        Form form = getVisibleForm(slug);
        assertSingleMode(form);
        User user = userService.getCurrentUser();

        return submissionRepository.findFirstByFormAndUserOrderByCreatedAtAsc(form, user)
                .map(submission -> MySubmissionDTO.from(submission, answerRepository.findBySubmission(submission)))
                .orElseGet(MySubmissionDTO::empty);
    }

    /**
     * Grava o estado completo das respostas de texto/escolha do usuário logado. Tudo ou nada: qualquer
     * violação desfaz a transação inteira. A visibilidade condicional é recalculada aqui, no servidor:
     * respostas de perguntas que ficaram ocultas são descartadas, mesmo que o cliente as tenha enviado.
     */
    @Transactional
    public MySubmissionDTO submit(String slug, SubmitFormRequestDTO request) {
        Form form = getOpenForm(slug);
        assertSingleMode(form);
        User user = userService.getCurrentUser();
        FormSubmission submission = submissionRepository.findFirstByFormAndUserOrderByCreatedAtAsc(form, user)
                .orElseGet(() -> createBareSubmission(form, user));
        assertEditable(form, submission);
        return applyAnswers(form, submission, request);
    }

    @Transactional
    public AnswerDTO uploadFile(String slug, String questionCode, MultipartFile file) throws IOException {
        Form form = getOpenForm(slug);
        assertSingleMode(form);
        FormQuestion question = getFileQuestion(form, questionCode);
        User user = userService.getCurrentUser();
        FormSubmission submission = submissionRepository.findFirstByFormAndUserOrderByCreatedAtAsc(form, user)
                .orElseGet(() -> createBareSubmission(form, user));
        return uploadFileToSubmission(form, submission, question, file);
    }

    /** Idempotente: sem arquivo anexado, não faz nada. */
    @Transactional
    public void removeFile(String slug, String questionCode) {
        Form form = getOpenForm(slug);
        assertSingleMode(form);
        FormQuestion question = getFileQuestion(form, questionCode);

        FormSubmission submission = submissionRepository
                .findFirstByFormAndUserOrderByCreatedAtAsc(form, userService.getCurrentUser())
                .orElse(null);
        if (submission == null) {
            return;
        }
        removeFileFromSubmission(form, submission, question);
    }

    // ── modo de múltiplas respostas ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MySubmissionDTO> listMySubmissions(String slug) {
        Form form = getVisibleForm(slug);
        assertMultiMode(form);
        User user = userService.getCurrentUser();

        return submissionRepository.findByFormAndUserOrderByCreatedAtAsc(form, user).stream()
                .map(s -> MySubmissionDTO.from(s, answerRepository.findBySubmission(s)))
                .toList();
    }

    @Transactional(readOnly = true)
    public MySubmissionDTO getMySubmissionEntry(String slug, UUID submissionId) {
        Form form = getVisibleForm(slug);
        assertMultiMode(form);
        FormSubmission submission = getOwnedSubmission(form, submissionId);
        return MySubmissionDTO.from(submission, answerRepository.findBySubmission(submission));
    }

    /**
     * Cria uma entrada em branco (sem respostas), para então anexar arquivos e/ou chamar
     * {@link #updateSubmissionEntry} — o mesmo desenho em duas fases do modo de resposta única, só que
     * aqui o id da entrada precisa ser explícito desde o início (não há como inferi-lo de form+usuário).
     */
    @Transactional
    public MySubmissionDTO createSubmissionEntry(String slug) {
        Form form = getOpenForm(slug);
        assertMultiMode(form);
        User user = userService.getCurrentUser();

        Integer max = form.getMaxSubmissionsPerUser();
        if (max != null && submissionRepository.countByFormAndUser(form, user) >= max) {
            throw new BusinessRuleException(
                    "Você já atingiu o número máximo de envios para este formulário (" + max + ").");
        }

        FormSubmission submission = createBareSubmission(form, user);
        return MySubmissionDTO.from(submission, List.of());
    }

    @Transactional
    public MySubmissionDTO updateSubmissionEntry(String slug, UUID submissionId, SubmitFormRequestDTO request) {
        Form form = getOpenForm(slug);
        assertMultiMode(form);
        FormSubmission submission = getOwnedSubmission(form, submissionId);
        assertEditable(form, submission);
        return applyAnswers(form, submission, request);
    }

    @Transactional
    public void deleteSubmissionEntry(String slug, UUID submissionId) {
        Form form = getOpenForm(slug);
        assertMultiMode(form);
        FormSubmission submission = getOwnedSubmission(form, submissionId);
        assertEditable(form, submission);

        List<FormAnswer> answers = answerRepository.findBySubmission(submission);
        List<String> fileUrls = answers.stream().map(FormAnswer::getFileUrl).filter(Objects::nonNull).toList();

        // Precisa vir antes do delete da submissão: não há coleção mapeada Submission->Answer para o
        // JPA cascatear sozinho, e o MySQL rejeitaria por FK se a ordem fosse invertida.
        answerRepository.deleteAll(answers);
        submissionRepository.delete(submission);
        deleteFilesAfterCommit(fileUrls);
    }

    @Transactional
    public AnswerDTO uploadFileToEntry(String slug, UUID submissionId, String questionCode, MultipartFile file) throws IOException {
        Form form = getOpenForm(slug);
        assertMultiMode(form);
        FormQuestion question = getFileQuestion(form, questionCode);
        FormSubmission submission = getOwnedSubmission(form, submissionId);
        return uploadFileToSubmission(form, submission, question, file);
    }

    @Transactional
    public void removeFileFromEntry(String slug, UUID submissionId, String questionCode) {
        Form form = getOpenForm(slug);
        assertMultiMode(form);
        FormQuestion question = getFileQuestion(form, questionCode);
        FormSubmission submission = getOwnedSubmission(form, submissionId);
        removeFileFromSubmission(form, submission, question);
    }

    // ── núcleo compartilhado ─────────────────────────────────────────────────

    private MySubmissionDTO applyAnswers(Form form, FormSubmission submission, SubmitFormRequestDTO request) {
        List<FormQuestion> questions = questionRepository.findByFormAndActiveTrueOrderByDisplayOrderAscIdAsc(form);
        Map<String, AnswerInputDTO> inputs = indexInputs(request.answers(), questions);
        Map<UUID, Map<String, FormOption>> optionsBySet = loadActiveOptionsBySet(questions);
        Map<UUID, FormAnswer> existing = answerRepository.findBySubmission(submission).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

        Set<UUID> visibleIds = new HashSet<>();
        Map<UUID, Set<UUID>> selected = new HashMap<>();
        List<String> filesToDelete = new ArrayList<>();

        for (FormQuestion question : questions) {
            FormAnswer current = existing.get(question.getId());

            if (!QuestionVisibility.isVisible(question, visibleIds, selected)) {
                if (current != null) {
                    if (current.getFileUrl() != null) {
                        filesToDelete.add(current.getFileUrl());
                    }
                    answerRepository.delete(current);
                }
                continue;
            }
            visibleIds.add(question.getId());

            if (question.getType() == QuestionType.FILE) {
                boolean hasFile = current != null && current.getFileUrl() != null;
                if (question.isRequired() && !hasFile) {
                    throw AnswerValidator.error(question, "resposta obrigatória.");
                }
                continue;
            }

            Map<String, FormOption> options = question.getOptionSet() == null
                    ? Map.of()
                    : optionsBySet.getOrDefault(question.getOptionSet().getId(), Map.of());
            AnswerValidator.Parsed parsed = AnswerValidator.parse(question, inputs.get(question.getCode()), options);

            if (parsed.isEmpty()) {
                if (question.isRequired()) {
                    throw AnswerValidator.error(question, "resposta obrigatória.");
                }
                if (current != null) {
                    answerRepository.delete(current);
                }
                continue;
            }

            FormAnswer answer = current != null ? current : newAnswer(submission, question);
            Set<UUID> chosen = apply(answer, question, parsed);
            answerRepository.save(answer);
            if (question.getType().isChoice()) {
                selected.put(question.getId(), chosen);
            }
        }

        if (submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(LocalDateTime.now());
        }
        submissionRepository.save(submission);
        deleteFilesAfterCommit(filesToDelete);

        return MySubmissionDTO.from(submission, answerRepository.findBySubmission(submission));
    }

    /** Anexa (ou substitui) o arquivo de uma pergunta FILE nesta entrada. */
    private AnswerDTO uploadFileToSubmission(Form form, FormSubmission submission, FormQuestion question, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("O arquivo é obrigatório.");
        }
        assertEditable(form, submission);
        FileAnswerValidator.validate(question, file);

        StoredFile stored = fileStorage.store(UploadRequest.of(file, DocumentKind.FORM_ATTACHMENT));

        FormAnswer answer = answerRepository.findBySubmissionAndQuestion(submission, question)
                .orElseGet(() -> newAnswer(submission, question));
        String previousUrl = answer.getFileUrl();
        answer.setFileUrl(stored.url());
        answer.setFileName(sanitizeFileName(file.getOriginalFilename()));
        answerRepository.save(answer);

        if (previousUrl != null) {
            deleteFilesAfterCommit(List.of(previousUrl));
        }
        return AnswerDTO.from(answer);
    }

    private void removeFileFromSubmission(Form form, FormSubmission submission, FormQuestion question) {
        assertEditable(form, submission);

        FormAnswer answer = answerRepository.findBySubmissionAndQuestion(submission, question).orElse(null);
        if (answer == null) {
            return;
        }
        if (question.isRequired() && submission.getSubmittedAt() != null) {
            throw AnswerValidator.error(question, "resposta obrigatória, o arquivo não pode ser removido (envie outro para substituí-lo).");
        }

        String url = answer.getFileUrl();
        answerRepository.delete(answer);
        if (url != null) {
            deleteFilesAfterCommit(List.of(url));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Rascunho é invisível; formulário encerrado continua legível. */
    private Form getVisibleForm(String slug) {
        return formRepository.findBySlug(slug)
                .filter(form -> form.getStatus() != FormStatus.DRAFT)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));
    }

    private Form getOpenForm(String slug) {
        Form form = getVisibleForm(slug);
        if (form.getStatus() != FormStatus.OPEN) {
            throw new BusinessRuleException("Este formulário não está aceitando respostas.");
        }
        return form;
    }

    private void assertSingleMode(Form form) {
        if (form.isAllowMultipleSubmissions()) {
            throw new BusinessRuleException("Este formulário aceita múltiplas respostas; use os endpoints de múltiplos envios (/submissions).");
        }
    }

    private void assertMultiMode(Form form) {
        if (!form.isAllowMultipleSubmissions()) {
            throw new BusinessRuleException("Este formulário não aceita múltiplas respostas.");
        }
    }

    private FormQuestion getFileQuestion(Form form, String questionCode) {
        return questionRepository.findByFormAndCode(form, questionCode)
                .filter(q -> q.isActive() && q.getType() == QuestionType.FILE)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta de arquivo inexistente."));
    }

    private FormSubmission createBareSubmission(Form form, User user) {
        FormSubmission submission = new FormSubmission();
        submission.setForm(form);
        submission.setUser(user);
        return submissionRepository.save(submission);
    }

    /** 404 em vez de 403 para não confirmar a um usuário que uma entrada de outro usuário existe. */
    private FormSubmission getOwnedSubmission(Form form, UUID submissionId) {
        return submissionRepository.findByIdAndFormAndUser(submissionId, form, userService.getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Envio não encontrado."));
    }

    private void assertEditable(Form form, FormSubmission submission) {
        if (submission.getSubmittedAt() != null && !form.isAllowEditAfterSubmit()) {
            throw new BusinessRuleException("Este formulário já foi respondido e não pode ser editado.");
        }
    }

    private Map<String, AnswerInputDTO> indexInputs(List<AnswerInputDTO> answers, List<FormQuestion> questions) {
        Map<String, FormQuestion> byCode = questions.stream()
                .collect(Collectors.toMap(FormQuestion::getCode, Function.identity()));

        Map<String, AnswerInputDTO> inputs = new HashMap<>();
        Set<String> unknown = new TreeSet<>();
        for (AnswerInputDTO input : answers) {
            FormQuestion question = byCode.get(input.question());
            if (question == null) {
                unknown.add(input.question());
                continue;
            }
            if (question.getType() == QuestionType.FILE) {
                throw AnswerValidator.error(question, "arquivos são enviados pelo endpoint de arquivos.");
            }
            if (inputs.put(input.question(), input) != null) {
                throw AnswerValidator.error(question, "resposta enviada mais de uma vez.");
            }
        }
        if (!unknown.isEmpty()) {
            throw new BusinessRuleException("Perguntas inexistentes: " + String.join(", ", unknown) + ".");
        }
        return inputs;
    }

    private Map<UUID, Map<String, FormOption>> loadActiveOptionsBySet(List<FormQuestion> questions) {
        Set<UUID> setIds = questions.stream()
                .filter(q -> q.getOptionSet() != null)
                .map(q -> q.getOptionSet().getId())
                .collect(Collectors.toSet());
        if (setIds.isEmpty()) {
            return Map.of();
        }

        return optionRepository.findByOptionSetIdInOrderByDisplayOrderAscIdAsc(setIds).stream()
                .filter(FormOption::isActive)
                .collect(Collectors.groupingBy(
                        o -> o.getOptionSet().getId(),
                        Collectors.toMap(FormOption::getCode, Function.identity())));
    }

    private FormAnswer newAnswer(FormSubmission submission, FormQuestion question) {
        FormAnswer answer = new FormAnswer();
        answer.setSubmission(submission);
        answer.setQuestion(question);
        return answer;
    }

    /**
     * Aplica a resposta validada. Nas escolhas, compara com o que já está salvo em vez de apagar e
     * recriar: o Hibernate insere antes de excluir e violaria a unicidade (resposta, opção).
     *
     * @return ids das opções marcadas (vazio para perguntas de texto)
     */
    private Set<UUID> apply(FormAnswer answer, FormQuestion question, AnswerValidator.Parsed parsed) {
        if (!question.getType().isChoice()) {
            answer.setTextValue(parsed.text());
            return Set.of();
        }

        Map<UUID, AnswerValidator.Choice> wanted = parsed.choices().stream()
                .collect(Collectors.toMap(c -> c.option().getId(), Function.identity(), (a, b) -> a, LinkedHashMap::new));

        answer.getOptions().removeIf(o -> !wanted.containsKey(o.getOption().getId()));
        Map<UUID, FormAnswerOption> kept = answer.getOptions().stream()
                .collect(Collectors.toMap(o -> o.getOption().getId(), Function.identity()));

        for (AnswerValidator.Choice choice : wanted.values()) {
            FormAnswerOption answerOption = kept.get(choice.option().getId());
            if (answerOption == null) {
                answerOption = new FormAnswerOption();
                answerOption.setAnswer(answer);
                answerOption.setOption(choice.option());
                answer.getOptions().add(answerOption);
            }
            answerOption.setFreeText(choice.freeText());
        }
        return wanted.keySet();
    }

    private static String sanitizeFileName(String original) {
        String name = original == null ? null : StringUtils.getFilename(StringUtils.cleanPath(original));
        if (name == null || name.isBlank()) {
            return "arquivo";
        }
        return name.length() > FILE_NAME_MAX_LENGTH ? name.substring(0, FILE_NAME_MAX_LENGTH) : name;
    }

    /** Só apaga do storage se a transação confirmar; um rollback não pode deixar o banco apontando para um arquivo apagado. */
    private void deleteFilesAfterCommit(Collection<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        List<String> toDelete = List.copyOf(urls);
        Runnable task = () -> toDelete.forEach(url -> {
            try {
                fileStorage.delete(url);
            } catch (RuntimeException e) {
                log.warn("Não foi possível remover o arquivo do storage: {}", url, e);
            }
        });

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }
}
