package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormAnswer;
import com.caco.sitedocaco.features.forms.entity.FormSubmission;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Uma entrada de resposta (em formulários de resposta única, a única que pode existir), com as
 * respostas indexadas pelo código da pergunta. {@code submitted} é falso enquanto a entrada não foi
 * enviada com sucesso (mesmo que já exista um arquivo anexado como rascunho).
 */
public record MySubmissionDTO(
        UUID id,
        boolean submitted,
        LocalDateTime submittedAt,
        Map<String, AnswerDTO> answers
) {
    public static MySubmissionDTO empty() {
        return new MySubmissionDTO(null, false, null, Map.of());
    }

    public static MySubmissionDTO from(FormSubmission submission, List<FormAnswer> answers) {
        Map<String, AnswerDTO> byCode = new LinkedHashMap<>();
        answers.stream()
                .filter(a -> a.getQuestion().isActive())
                .sorted(Comparator.comparingInt(a -> a.getQuestion().getDisplayOrder()))
                .forEach(a -> byCode.put(a.getQuestion().getCode(), AnswerDTO.from(a)));

        return new MySubmissionDTO(submission.getId(), submission.getSubmittedAt() != null, submission.getSubmittedAt(), byCode);
    }
}
