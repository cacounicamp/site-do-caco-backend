package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.CreateFormQuestionDTO;
import com.caco.sitedocaco.features.forms.dto.request.QuestionConfig;
import com.caco.sitedocaco.features.forms.dto.request.ReorderFormQuestionsDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateFormQuestionDTO;
import com.caco.sitedocaco.features.forms.dto.response.QuestionAdminDTO;
import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.OptionSet;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import com.caco.sitedocaco.features.forms.repository.FormAnswerRepository;
import com.caco.sitedocaco.features.forms.repository.FormOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.FormRepository;
import com.caco.sitedocaco.features.forms.repository.OptionSetRepository;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormQuestionAdminService {

    private final FormRepository formRepository;
    private final FormQuestionRepository questionRepository;
    private final FormOptionRepository optionRepository;
    private final OptionSetRepository optionSetRepository;
    private final FormAnswerRepository answerRepository;

    @Transactional
    public QuestionAdminDTO create(UUID formId, CreateFormQuestionDTO dto) {
        Form form = getForm(formId);
        if (questionRepository.existsByFormAndCode(form, dto.code())) {
            throw new BusinessRuleException("Já existe uma pergunta com o código '" + dto.code() + "' neste formulário.");
        }

        FormQuestion question = new FormQuestion();
        question.setForm(form);
        question.setCode(dto.code());
        Integer maxOrder = questionRepository.findMaxDisplayOrder(form);
        question.setDisplayOrder(maxOrder == null ? 0 : maxOrder + 1);

        apply(question, dto, true);
        return QuestionAdminDTO.from(questionRepository.save(question), false);
    }

    @Transactional
    public QuestionAdminDTO update(UUID formId, UUID questionId, UpdateFormQuestionDTO dto) {
        FormQuestion question = getQuestion(formId, questionId);

        apply(question, dto, false);
        return QuestionAdminDTO.from(questionRepository.save(question), answerRepository.existsByQuestion(question));
    }

    @Transactional
    public void delete(UUID formId, UUID questionId) {
        FormQuestion question = getQuestion(formId, questionId);

        if (answerRepository.existsByQuestion(question)) {
            throw new BusinessRuleException("Pergunta com respostas não pode ser excluída. Desative-a (active=false) em vez disso.");
        }
        if (questionRepository.existsByShowIfQuestion(question)) {
            throw new BusinessRuleException("Outras perguntas dependem desta. Remova as condicionais delas antes de excluí-la.");
        }
        questionRepository.delete(question);
    }

    /** Reatribui a ordem de todas as perguntas; falha se alguma condicional passar a vir antes da sua pergunta-mãe. */
    @Transactional
    public List<QuestionAdminDTO> reorder(UUID formId, ReorderFormQuestionsDTO dto) {
        Form form = getForm(formId);
        List<FormQuestion> questions = questionRepository.findByFormOrderByDisplayOrderAscIdAsc(form);

        Set<UUID> expected = questions.stream().map(FormQuestion::getId).collect(Collectors.toSet());
        List<UUID> requested = dto.questionIds();
        if (requested.size() != expected.size() || !expected.equals(new HashSet<>(requested))) {
            throw new BusinessRuleException("A lista deve conter exatamente todas as perguntas do formulário, sem repetições.");
        }

        Map<UUID, Integer> newOrder = new HashMap<>();
        for (int i = 0; i < requested.size(); i++) {
            newOrder.put(requested.get(i), i);
        }

        for (FormQuestion question : questions) {
            FormQuestion parent = question.getShowIfQuestion();
            if (parent != null && newOrder.get(parent.getId()) >= newOrder.get(question.getId())) {
                throw new BusinessRuleException("A pergunta '" + question.getCode() + "' depende de '"
                        + parent.getCode() + "', que precisa vir antes dela.");
            }
        }

        questions.forEach(q -> q.setDisplayOrder(newOrder.get(q.getId())));
        questionRepository.saveAll(questions);

        Set<UUID> withAnswers = Set.copyOf(answerRepository.findQuestionIdsWithAnswers(form));
        return questions.stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(q -> QuestionAdminDTO.from(q, withAnswers.contains(q.getId())))
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void apply(FormQuestion question, QuestionConfig config, boolean isNew) {
        QuestionType type = config.type();
        OptionSet optionSet = resolveOptionSet(type, config.optionSetSlug());
        validateTypeSpecificConfig(type, config);

        boolean active = config.active() == null ? (isNew || question.isActive()) : config.active();

        if (!isNew) {
            boolean structuralChange = question.getType() != type
                    || !Objects.equals(idOf(question.getOptionSet()), idOf(optionSet));
            if (structuralChange && answerRepository.existsByQuestion(question)) {
                throw new BusinessRuleException("Esta pergunta já tem respostas: não é possível alterar o tipo nem o conjunto de opções.");
            }
            if (structuralChange && questionRepository.existsByShowIfQuestion(question)) {
                throw new BusinessRuleException("Outras perguntas dependem desta: remova as condicionais delas antes de alterar o tipo ou o conjunto de opções.");
            }
            if (question.isActive() && !active && questionRepository.existsByShowIfQuestionAndActiveTrue(question)) {
                throw new BusinessRuleException("Há perguntas ativas que dependem desta. Desative-as antes.");
            }
        }

        question.setPrompt(config.prompt().strip());
        question.setHelpText(blankToNull(config.helpText()));
        question.setType(type);
        question.setOptionSet(optionSet);
        question.setRequired(Boolean.TRUE.equals(config.required()));
        question.setActive(active);
        question.setSection(blankToNull(config.section()));
        question.setTextFormat(config.textFormat());
        question.setMinValue(config.minValue());
        question.setMaxValue(config.maxValue());
        resolveShowIf(question, config);
    }

    private OptionSet resolveOptionSet(QuestionType type, String slug) {
        String optionSetSlug = blankToNull(slug);
        if (!type.isChoice()) {
            if (optionSetSlug != null) {
                throw new BusinessRuleException("Só perguntas de escolha usam conjunto de opções.");
            }
            return null;
        }
        if (optionSetSlug == null) {
            throw new BusinessRuleException("Perguntas de escolha exigem um conjunto de opções (optionSetSlug).");
        }
        return optionSetRepository.findBySlug(optionSetSlug)
                .orElseThrow(() -> new BusinessRuleException("Conjunto de opções inexistente: " + optionSetSlug + "."));
    }

    private void validateTypeSpecificConfig(QuestionType type, QuestionConfig config) {
        if (config.textFormat() != null && type != QuestionType.TEXT) {
            throw new BusinessRuleException("O formato só se aplica a perguntas de texto curto (TEXT).");
        }
        if (!type.isText() && (config.minValue() != null || config.maxValue() != null)) {
            throw new BusinessRuleException("Mínimo e máximo só se aplicam a perguntas de texto.");
        }
        if (config.minValue() != null && config.maxValue() != null && config.minValue() > config.maxValue()) {
            throw new BusinessRuleException("O mínimo não pode ser maior que o máximo.");
        }

        // Com formato INTEGER, min/max são valores; nos demais, são quantidades de caracteres.
        if (type.isText() && config.textFormat() != TextFormat.INTEGER) {
            int limit = type == QuestionType.LONG_TEXT ? AnswerValidator.LONG_TEXT_MAX_LENGTH : AnswerValidator.TEXT_MAX_LENGTH;
            if (config.minValue() != null && config.minValue() < 0) {
                throw new BusinessRuleException("O mínimo de caracteres não pode ser negativo.");
            }
            if (config.maxValue() != null && (config.maxValue() < 1 || config.maxValue() > limit)) {
                throw new BusinessRuleException("O máximo de caracteres deve estar entre 1 e " + limit + ".");
            }
        }
    }

    /** Condicional: precisa apontar para uma pergunta de escolha do mesmo formulário que venha ANTES desta. */
    private void resolveShowIf(FormQuestion question, QuestionConfig config) {
        String parentCode = blankToNull(config.showIfQuestion());
        String optionCode = blankToNull(config.showIfOption());

        if (parentCode == null) {
            if (optionCode != null) {
                throw new BusinessRuleException("Informe a pergunta da condicional (showIfQuestion).");
            }
            question.setShowIfQuestion(null);
            question.setShowIfOption(null);
            return;
        }

        FormQuestion parent = questionRepository.findByFormAndCode(question.getForm(), parentCode)
                .orElseThrow(() -> new BusinessRuleException("Pergunta da condicional não encontrada: " + parentCode + "."));
        if (Objects.equals(parent.getId(), question.getId())) {
            throw new BusinessRuleException("Uma pergunta não pode depender de si mesma.");
        }
        if (!parent.getType().isChoice()) {
            throw new BusinessRuleException("A pergunta da condicional precisa ser de escolha.");
        }
        if (parent.getDisplayOrder() >= question.getDisplayOrder()) {
            throw new BusinessRuleException("A pergunta da condicional precisa vir antes desta (ordem menor).");
        }

        FormOption trigger = null;
        if (optionCode != null) {
            trigger = optionRepository.findByOptionSetIdAndCode(parent.getOptionSet().getId(), optionCode)
                    .orElseThrow(() -> new BusinessRuleException(
                            "A opção '" + optionCode + "' não pertence às opções de '" + parentCode + "'."));
        }
        question.setShowIfQuestion(parent);
        question.setShowIfOption(trigger);
    }

    private Form getForm(UUID formId) {
        return formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));
    }

    private FormQuestion getQuestion(UUID formId, UUID questionId) {
        return questionRepository.findByIdAndForm(questionId, getForm(formId))
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta não encontrada."));
    }

    private static UUID idOf(OptionSet optionSet) {
        return optionSet == null ? null : optionSet.getId();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
