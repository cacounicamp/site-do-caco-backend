package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.response.FormStatisticsDTO;
import com.caco.sitedocaco.features.forms.dto.response.OptionStatDTO;
import com.caco.sitedocaco.features.forms.dto.response.QuestionStatisticsDTO;
import com.caco.sitedocaco.features.forms.entity.Form;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import com.caco.sitedocaco.features.forms.repository.FormAnswerOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormAnswerRepository;
import com.caco.sitedocaco.features.forms.repository.FormOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.FormRepository;
import com.caco.sitedocaco.features.forms.repository.FormSubmissionRepository;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Visão estatística simples por formulário — uma pergunta de cada vez, sem cruzamento entre elas.
 * Consultas customizadas (filtros compostos, distribuição por tempo etc.) ficam para um motor de
 * coorte à parte; aqui é só "quantas respostas cada opção teve".
 */
@Service
@RequiredArgsConstructor
public class FormStatisticsService {

    private final FormRepository formRepository;
    private final FormQuestionRepository questionRepository;
    private final FormOptionRepository optionRepository;
    private final FormSubmissionRepository submissionRepository;
    private final FormAnswerRepository answerRepository;
    private final FormAnswerOptionRepository answerOptionRepository;

    @Transactional(readOnly = true)
    public FormStatisticsDTO getStatistics(UUID formId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));

        // Perguntas inativas continuam aqui: desativar uma pergunta não deve apagar o histórico dela.
        List<FormQuestion> questions = questionRepository.findByFormOrderByDisplayOrderAscIdAsc(form);

        Map<UUID, Long> answeredByQuestion = answerRepository.countAnsweredGroupedByQuestionForForm(form).stream()
                .collect(Collectors.toMap(
                        FormAnswerRepository.QuestionAnswerCount::getQuestionId,
                        FormAnswerRepository.QuestionAnswerCount::getTotal));

        Map<UUID, Map<UUID, Long>> optionCountsByQuestion = new HashMap<>();
        for (FormAnswerOptionRepository.OptionCount row : answerOptionRepository.countGroupedByOptionForForm(form)) {
            optionCountsByQuestion
                    .computeIfAbsent(row.getQuestionId(), k -> new HashMap<>())
                    .put(row.getOptionId(), row.getTotal());
        }

        List<UUID> integerQuestionIds = questions.stream()
                .filter(q -> q.getType() == QuestionType.TEXT && q.getTextFormat() == TextFormat.INTEGER)
                .map(FormQuestion::getId)
                .toList();
        Map<UUID, List<FormAnswerRepository.TextValueCount>> textValuesByQuestion = integerQuestionIds.isEmpty()
                ? Map.of()
                : answerRepository.countTextValuesGroupedByQuestion(integerQuestionIds).stream()
                        .collect(Collectors.groupingBy(FormAnswerRepository.TextValueCount::getQuestionId));

        Set<UUID> optionSetIds = questions.stream()
                .filter(q -> q.getOptionSet() != null)
                .map(q -> q.getOptionSet().getId())
                .collect(Collectors.toSet());
        // Todas as opções (não só ativas): uma opção desativada com respostas históricas não pode sumir da estatística.
        Map<UUID, List<FormOption>> optionsBySet = optionSetIds.isEmpty() ? Map.of()
                : optionRepository.findByOptionSetIdInOrderByDisplayOrderAscIdAsc(optionSetIds).stream()
                        .collect(Collectors.groupingBy(o -> o.getOptionSet().getId()));

        List<QuestionStatisticsDTO> questionStats = questions.stream()
                .map(q -> buildQuestionStatistics(q, answeredByQuestion, optionCountsByQuestion, textValuesByQuestion, optionsBySet))
                .toList();

        return new FormStatisticsDTO(
                form.getSlug(),
                form.getName(),
                submissionRepository.countByFormAndSubmittedAtIsNotNull(form),
                submissionRepository.countDistinctRespondentsByForm(form),
                questionStats
        );
    }

    private QuestionStatisticsDTO buildQuestionStatistics(
            FormQuestion question,
            Map<UUID, Long> answeredByQuestion,
            Map<UUID, Map<UUID, Long>> optionCountsByQuestion,
            Map<UUID, List<FormAnswerRepository.TextValueCount>> textValuesByQuestion,
            Map<UUID, List<FormOption>> optionsBySet
    ) {
        long answered = answeredByQuestion.getOrDefault(question.getId(), 0L);
        List<OptionStatDTO> distribution = List.of();

        if (question.getType().isChoice() && question.getOptionSet() != null) {
            Map<UUID, Long> counts = optionCountsByQuestion.getOrDefault(question.getId(), Map.of());
            distribution = optionsBySet.getOrDefault(question.getOptionSet().getId(), List.of()).stream()
                    .filter(o -> o.isActive() || counts.containsKey(o.getId()))
                    .map(o -> new OptionStatDTO(o.getCode(), o.getLabel(), counts.getOrDefault(o.getId(), 0L)))
                    .toList();
        } else if (question.getType() == QuestionType.TEXT && question.getTextFormat() == TextFormat.INTEGER) {
            distribution = textValuesByQuestion.getOrDefault(question.getId(), List.of()).stream()
                    .sorted(Comparator.comparingInt(v -> Integer.parseInt(v.getValue())))
                    .map(v -> new OptionStatDTO(v.getValue(), v.getValue(), v.getTotal()))
                    .toList();
        }

        return new QuestionStatisticsDTO(question.getCode(), question.getPrompt(), question.getType(), answered, distribution);
    }
}
