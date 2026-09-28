package com.caco.sitedocaco.features.forms.dto.request;

import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;

import java.util.Set;

/** Campos de configuração de pergunta comuns à criação e à atualização. */
public interface QuestionConfig {
    String prompt();
    String helpText();
    QuestionType type();
    Boolean required();
    Boolean active();
    String section();
    String optionSetSlug();
    TextFormat textFormat();
    Integer minValue();
    Integer maxValue();
    /** Código da pergunta-mãe da condicional. */
    String showIfQuestion();
    /** Código da opção que dispara a condicional; nulo = qualquer opção marcada na pergunta-mãe. */
    String showIfOption();
    /** Só para FILE. Vazio/nulo = aceita qualquer extensão do catálogo suportado. */
    Set<String> allowedExtensions();
    /** Só para FILE. Nulo = usa o padrão (3MB). */
    Integer maxFileSizeBytes();
}
