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
 * Uma linha por entrada respondida. Em {@link Form#isAllowMultipleSubmissions()}=false a unicidade
 * de (formulário, usuário) é imposta em {@code FormSubmissionService}, não pelo banco: com múltiplas
 * respostas habilitadas, várias linhas por (formulário, usuário) são o estado normal, então não há
 * unique constraint aqui. Pode existir "rascunho" (submittedAt nulo) quando o usuário enviou um
 * arquivo antes de concluir a entrada; só conta como respondida após o primeiro envio válido.
 */
@Entity
@Getter
@Setter
@Table(name = "form_submission")
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
