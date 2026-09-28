package com.caco.sitedocaco.features.forms.dto.response;

/**
 * Um balde da distribuição de uma pergunta: para escolha, {@code value} é o código da opção e
 * {@code label} seu rótulo; para texto com formato inteiro, ambos são o próprio valor observado.
 */
public record OptionStatDTO(String value, String label, long count) {
}
