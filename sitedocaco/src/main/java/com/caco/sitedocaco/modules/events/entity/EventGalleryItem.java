package com.caco.sitedocaco.modules.events.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table
@Data
public class EventGalleryItem {
    /*
    2. **`EventGalleryItem`**
        - `UUID id`
        - `Event event` (ManyToOne)
        - `String mediaUrl`
        - `MediaType type` (Enum: `IMAGE`, `VIDEO`)
    */

    public enum MediaType {
        IMAGE,
        VIDEO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    Event event;

    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(columnDefinition = "TEXT")
    private String caption;
}
