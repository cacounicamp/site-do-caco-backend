package com.caco.sitedocaco.features.forms.dto.response;

import java.util.List;

/**
 * Visão geral das respostas de um formulário. {@code totalSubmissions} conta entradas enviadas
 * (em formulários de múltiplas respostas, cada entrada conta separadamente); {@code uniqueRespondents}
 * conta pessoas distintas — nos de resposta única os dois números coincidem.
 */
public record FormStatisticsDTO(
        String slug,
        String name,
        long totalSubmissions,
        long uniqueRespondents,
        List<QuestionStatisticsDTO> questions
) {
}
