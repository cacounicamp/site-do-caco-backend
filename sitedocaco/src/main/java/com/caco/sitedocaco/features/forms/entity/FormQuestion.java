package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "form_question",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_question_form_code", columnNames = {"form_id", "code"})
)
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    /** Identificador estável dentro do formulário; é a chave usada pelo front nas respostas. */
    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 300)
    private String prompt;

    @Column(length = 500)
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    /** Só para SINGLE_CHOICE / MULTIPLE_CHOICE. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_set_id")
    private OptionSet optionSet;

    @Column(nullable = false)
    private boolean required = false;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    /** Agrupamento apenas visual: o front usa para separar em passos/seções. */
    @Column(length = 60)
    private String section;

    /**
     * Condicional: "só apareça se, NA pergunta X, a opção Y estiver marcada".
     * Precisa das duas pontas, pois um mesmo conjunto de opções (ex.: sim/não) é usado por várias
     * perguntas. {@code showIfOption == null} significa "qualquer opção marcada em X".
     * A pergunta X precisa ter displayOrder menor que esta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_if_question_id")
    private FormQuestion showIfQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_if_option_id")
    private FormOption showIfOption;

    /** Só para TEXT. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TextFormat textFormat;

    /** TEXT/LONG_TEXT: nº mínimo de caracteres; TEXT com formato INTEGER: valor mínimo. */
    private Integer minValue;

    /** Idem {@link #minValue}, para o máximo. */
    private Integer maxValue;
}
