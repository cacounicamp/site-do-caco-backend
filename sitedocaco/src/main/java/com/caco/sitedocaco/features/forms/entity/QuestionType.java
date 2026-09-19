package com.caco.sitedocaco.features.forms.entity;

/**
 * Não existe tipo "sim/não": é uma escolha única sobre um conjunto de opções (ex.: "sim-nao").
 * Assim toda condicional é "tal opção foi marcada", um caso só.
 */
public enum QuestionType {
    TEXT,
    LONG_TEXT,
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    FILE;

    public boolean isChoice() {
        return this == SINGLE_CHOICE || this == MULTIPLE_CHOICE;
    }

    public boolean isText() {
        return this == TEXT || this == LONG_TEXT;
    }
}
