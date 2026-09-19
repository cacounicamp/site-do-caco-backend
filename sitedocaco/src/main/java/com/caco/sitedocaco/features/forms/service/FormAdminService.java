package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.CreateFormDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateFormDTO;
import com.caco.sitedocaco.features.forms.dto.response.FormAdminDTO;
import com.caco.sitedocaco.features.forms.dto.response.FormSummaryAdminDTO;
import com.caco.sitedocaco.features.forms.dto.response.QuestionAdminDTO;
import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.FormStatus;
import com.caco.sitedocaco.features.forms.repository.FormAnswerRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.FormRepository;
import com.caco.sitedocaco.features.forms.repository.FormSubmissionRepository;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormAdminService {

    private final FormRepository formRepository;
    private final FormQuestionRepository questionRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormAnswerRepository answerRepository;

    @Transactional(readOnly = true)
    public List<FormSummaryAdminDTO> list() {
        return formRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(form -> FormSummaryAdminDTO.from(form, submissionRepository.countByForm(form)))
                .toList();
    }

    @Transactional(readOnly = true)
    public FormAdminDTO get(UUID id) {
        return toAdminDTO(getForm(id));
    }

    @Transactional
    public FormAdminDTO create(CreateFormDTO dto) {
        if (formRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe um formulário com esse slug.");
        }

        Form form = new Form();
        form.setName(dto.name().strip());
        form.setSlug(dto.slug());
        form.setDescription(dto.description());
        form.setAllowEditAfterSubmit(dto.allowEditAfterSubmit() == null || dto.allowEditAfterSubmit());
        return toAdminDTO(formRepository.save(form));
    }

    @Transactional
    public FormAdminDTO update(UUID id, UpdateFormDTO dto) {
        Form form = getForm(id);
        boolean hasSubmissions = submissionRepository.existsByForm(form);

        if (!form.getSlug().equals(dto.slug())) {
            if (hasSubmissions) {
                throw new BusinessRuleException("O slug não pode mudar depois que o formulário recebeu respostas.");
            }
            if (formRepository.existsBySlugAndIdNot(dto.slug(), form.getId())) {
                throw new BusinessRuleException("Já existe um formulário com esse slug.");
            }
        }

        if (dto.status() != form.getStatus()) {
            validateStatusChange(form, dto.status(), hasSubmissions);
        }

        form.setName(dto.name().strip());
        form.setSlug(dto.slug());
        form.setDescription(dto.description());
        form.setStatus(dto.status());
        form.setAllowEditAfterSubmit(dto.allowEditAfterSubmit());
        return toAdminDTO(formRepository.save(form));
    }

    @Transactional
    public void delete(UUID id) {
        Form form = getForm(id);
        if (submissionRepository.existsByForm(form)) {
            throw new BusinessRuleException("Formulário com respostas não pode ser excluído. Encerre-o (status CLOSED) em vez disso.");
        }

        // Desfaz as condicionais antes: as perguntas se referenciam entre si e a ordem de exclusão não é garantida.
        List<FormQuestion> questions = questionRepository.findByFormOrderByDisplayOrderAscIdAsc(form);
        questions.forEach(q -> {
            q.setShowIfQuestion(null);
            q.setShowIfOption(null);
        });
        questionRepository.saveAllAndFlush(questions);
        questionRepository.deleteAll(questions);
        formRepository.delete(form);
    }

    private void validateStatusChange(Form form, FormStatus target, boolean hasSubmissions) {
        if (target == FormStatus.OPEN && questionRepository.countByFormAndActiveTrue(form) == 0) {
            throw new BusinessRuleException("Adicione ao menos uma pergunta ativa antes de abrir o formulário.");
        }
        if (target == FormStatus.DRAFT && hasSubmissions) {
            throw new BusinessRuleException("Formulário com respostas não pode voltar para rascunho. Use o status CLOSED.");
        }
    }

    private Form getForm(UUID id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));
    }

    private FormAdminDTO toAdminDTO(Form form) {
        List<FormQuestion> questions = questionRepository.findByFormOrderByDisplayOrderAscIdAsc(form);
        Set<UUID> withAnswers = Set.copyOf(answerRepository.findQuestionIdsWithAnswers(form));

        List<QuestionAdminDTO> questionDTOs = questions.stream()
                .map(q -> QuestionAdminDTO.from(q, withAnswers.contains(q.getId())))
                .toList();
        return FormAdminDTO.from(form, submissionRepository.countByForm(form), questionDTOs);
    }
}
