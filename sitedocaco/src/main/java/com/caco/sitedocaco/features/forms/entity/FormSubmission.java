package com.caco.sitedocaco.features.forms.entity;

import com.caco.sitedocaco.features.users.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma linha por (formulário, usuário). Pode existir "rascunho" (submittedAt nulo) quando o usuário
 * enviou um arquivo antes de concluir o formulário; só conta como respondido após o primeiro envio válido.
 */
@Entity
@Getter
@Setter
@Table(
        name = "form_submission",
        uniqueConstraints = @UniqueConstraint(name = "uk_form_submission_form_user", columnNames = {"form_id", "user_id"})
)
public class FormSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime submittedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
