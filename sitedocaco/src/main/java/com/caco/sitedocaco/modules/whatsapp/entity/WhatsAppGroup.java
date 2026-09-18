package com.caco.sitedocaco.modules.whatsapp.entity;

import com.caco.sitedocaco.shared.entity.CourseType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(
        name = "whatsapp_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course", "entry_year"})
)
public class WhatsAppGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseType course;

    @Column(name = "entry_year", nullable = false)
    private int entryYear;

    @Column(nullable = false, length = 512)
    private String whatsappLink;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

