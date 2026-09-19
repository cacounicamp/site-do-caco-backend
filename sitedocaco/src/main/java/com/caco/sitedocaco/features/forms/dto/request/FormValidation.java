package com.caco.sitedocaco.features.forms.dto.request;

public final class FormValidation {

    /** Slug de URL: minúsculas, dígitos e hífens (ex.: "inscricao-secomp-2026"). */
    public static final String SLUG_REGEX = "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    public static final String SLUG_MESSAGE = "Use apenas letras minúsculas, números e hífens (ex.: inscricao-2026).";

    /** Códigos de pergunta/opção e slug de conjunto de opções: também aceitam underscore. */
    public static final String CODE_REGEX = "^[a-z0-9][a-z0-9_-]*$";
    public static final String CODE_MESSAGE = "Use apenas letras minúsculas, números, hífens e underscores.";

    private FormValidation() {
    }
}
