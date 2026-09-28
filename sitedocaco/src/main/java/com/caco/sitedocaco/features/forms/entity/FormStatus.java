package com.caco.sitedocaco.features.forms.entity;

public enum FormStatus {
    /** Em construção: invisível para o público e não aceita respostas. */
    DRAFT,
    /** Publicado e aceitando respostas. */
    OPEN,
    /** Continua visível (e o usuário ainda lê as próprias respostas), mas não aceita mais envios. */
    CLOSED
}
