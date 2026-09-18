package com.caco.sitedocaco.modules.users.entity;

import com.caco.sitedocaco.shared.entity.CourseType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Curso principal do usuário.
     * CIENCIAS_DA_COMPUTACAO, ENGENHARIA_DA_COMPUTACAO ou OUTRO.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseType course;

    /**
     * Preenchido apenas quando course == OUTRO.
     * Máximo de 50 caracteres.
     */
    @Column(length = 50)
    private String otherCourseName;

    /**
     * Ano de ingresso do aluno.
     * Valores válidos: 2018 – ano atual.
     * Qualquer valor < 2018 recebido pelo backend é salvo como -1.
     */
    @Column(nullable = false)
    private int entryYear;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}

