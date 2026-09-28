package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resposta a uma pergunta: texto, ou opções marcadas (options), ou arquivo, conforme o tipo. */
@Entity
@Getter
@Setter
@Table(
        name = "form_answer",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_answer_submission_question", columnNames = {"submission_id", "question_id"})
)
public class FormAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private FormSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestion question;

    @Column(length = 2000)
    private String textValue;

    @Column(length = 1024)
    private String fileUrl;

    @Column(length = 255)
    private String fileName;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormAnswerOption> options = new ArrayList<>();
}
