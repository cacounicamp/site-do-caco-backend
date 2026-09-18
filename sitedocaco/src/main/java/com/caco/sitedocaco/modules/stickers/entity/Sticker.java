package com.caco.sitedocaco.modules.stickers.entity;

import com.caco.sitedocaco.modules.events.entity.Event;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Data
public class Sticker {
    /*
    1. **`Sticker`**
        - `UUID id`
        - `String name`
        - `String description`
        - `String imageUrl`
        - `Event originEvent` (ManyToOne Nullable)
        - `LocalDateTime createdAt`
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 600)
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "origin_event_id")
    private Event originEvent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
