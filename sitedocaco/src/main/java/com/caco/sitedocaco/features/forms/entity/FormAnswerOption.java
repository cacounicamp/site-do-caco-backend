package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Uma linha por opção marcada. Tabela própria (e não uma lista em texto) para permitir agregações futuras. */
@Entity
@Getter
@Setter
@Table(
        name = "form_answer_option",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_answer_option_answer_option", columnNames = {"answer_id", "option_id"})
)
public class FormAnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private FormAnswer answer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private FormOption option;

    /** Preenchido só quando a opção é do tipo "Outro (especifique)". */
    @Column(length = 200)
    private String freeText;
}
