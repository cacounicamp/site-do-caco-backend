package com.caco.sitedocaco.features.forms.dto.response;

import com.caco.sitedocaco.features.forms.entity.QuestionType;

import java.util.List;

/**
 * Estatística simples de UMA pergunta, sobre respostas submetidas (rascunhos não contam).
 *
 * <p>{@code distribution} vem preenchida para escolha única/múltipla (todas as opções ativas do
 * conjunto, com contagem zero para as não marcadas; opções desativadas só aparecem se têm respostas
 * históricas) e para texto com formato inteiro (um balde por valor observado, ordenado). Para os
 * demais tipos (texto livre, arquivo) vem vazia — {@code answeredCount} já basta ali.
 *
 * <p>{@code answeredCount} não leva em conta visibilidade condicional: uma pergunta que só aparece
 * dependendo de outra resposta não tem, aqui, um "total de vezes que era aplicável" — isso é análise
 * de coorte, fora do escopo desta consulta.
 */
public record QuestionStatisticsDTO(
        String code,
        String prompt,
        QuestionType type,
        long answeredCount,
        List<OptionStatDTO> distribution
) {
}
