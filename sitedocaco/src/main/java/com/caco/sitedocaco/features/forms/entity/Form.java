package com.caco.sitedocaco.features.forms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "form", uniqueConstraints = @UniqueConstraint(name = "uk_form_slug", columnNames = "slug"))
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String slug;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormStatus status = FormStatus.DRAFT;

    /** Se false, depois do primeiro envio as respostas ficam travadas. */
    @Column(nullable = false)
    private boolean allowEditAfterSubmit = true;

    /**
     * Se true, um usuário pode ter várias {@link FormSubmission} (entradas) neste formulário, cada
     * uma com seu próprio ciclo de vida (ver {@link #maxSubmissionsPerUser}); cada entrada continua
     * sujeita a {@link #allowEditAfterSubmit} individualmente. Se false (padrão), é o modelo de
     * resposta única de sempre: no máximo uma submissão por (formulário, usuário).
     */
    @Column(nullable = false)
    private boolean allowMultipleSubmissions = false;

    /** Só relevante com {@link #allowMultipleSubmissions}=true. Nulo = sem limite. */
    private Integer maxSubmissionsPerUser;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
