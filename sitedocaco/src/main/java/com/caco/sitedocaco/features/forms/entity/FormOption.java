package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "form_option",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_option_set_code", columnNames = {"option_set_id", "code"})
)
public class FormOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_set_id", nullable = false)
    private OptionSet optionSet;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(nullable = false)
    private int displayOrder;

    /** Opção usada em respostas nunca é apagada: só desativada, para não perder o histórico. */
    @Column(nullable = false)
    private boolean active = true;

    /** "Outro (especifique)": o usuário precisa digitar um texto ao marcar esta opção. */
    @Column(nullable = false)
    private boolean allowsFreeText = false;
}
