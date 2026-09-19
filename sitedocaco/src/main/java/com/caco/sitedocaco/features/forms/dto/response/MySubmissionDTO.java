package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.FormAnswer;
import com.caco.sitedocaco.features.forms.entity.FormSubmission;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Respostas do usuário logado, indexadas pelo código da pergunta. {@code submitted} é falso enquanto
 * o formulário não foi enviado com sucesso (mesmo que já exista um arquivo anexado).
 */
public record MySubmissionDTO(
        boolean submitted,
        LocalDateTime submittedAt,
        Map<String, AnswerDTO> answers
) {
    public static MySubmissionDTO empty() {
        return new MySubmissionDTO(false, null, Map.of());
    }

    public static MySubmissionDTO from(FormSubmission submission, List<FormAnswer> answers) {
        Map<String, AnswerDTO> byCode = new LinkedHashMap<>();
        answers.stream()
                .filter(a -> a.getQuestion().isActive())
                .sorted(Comparator.comparingInt(a -> a.getQuestion().getDisplayOrder()))
                .forEach(a -> byCode.put(a.getQuestion().getCode(), AnswerDTO.from(a)));

        return new MySubmissionDTO(submission.getSubmittedAt() != null, submission.getSubmittedAt(), byCode);
    }
}
