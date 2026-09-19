package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.response.FormDTO;
import com.caco.sitedocaco.features.forms.dto.response.QuestionDTO;
import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.FormStatus;
import com.caco.sitedocaco.features.forms.repository.FormOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.FormRepository;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormPublicService {

    private final FormRepository formRepository;
    private final FormQuestionRepository questionRepository;
    private final FormOptionRepository optionRepository;

    /** Definição do formulário para o front renderizar. Rascunhos não existem para o público. */
    @Transactional(readOnly = true)
    public FormDTO getForm(String slug) {
        Form form = formRepository.findBySlug(slug)
                .filter(f -> f.getStatus() != FormStatus.DRAFT)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));

        List<FormQuestion> questions = questionRepository.findByFormAndActiveTrueOrderByDisplayOrderAscIdAsc(form);

        Set<UUID> setIds = questions.stream()
                .filter(q -> q.getOptionSet() != null)
                .map(q -> q.getOptionSet().getId())
                .collect(Collectors.toSet());
        Map<UUID, List<FormOption>> activeOptionsBySet = setIds.isEmpty() ? Map.of()
                : optionRepository.findByOptionSetIdInOrderByDisplayOrderAscIdAsc(setIds).stream()
                .filter(FormOption::isActive)
                .collect(Collectors.groupingBy(o -> o.getOptionSet().getId()));

        List<QuestionDTO> questionDTOs = questions.stream()
                .map(q -> QuestionDTO.from(q, q.getOptionSet() == null
                        ? List.of()
                        : activeOptionsBySet.getOrDefault(q.getOptionSet().getId(), List.of())))
                .toList();

        return FormDTO.from(form, questionDTOs);
    }
}
