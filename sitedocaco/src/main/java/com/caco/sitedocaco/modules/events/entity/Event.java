package com.caco.sitedocaco.modules.events.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class Event {
    /*
    1. **`Event`**
        - `UUID id`
        - `String title`
        - `String slug` (único)
        - `String description` (Markdown)
        - `LocalDateTime startDate`
        - `LocalDateTime endDate`
        - `String location`
        - `String locationUrl` (URL para o mapa da localização)
        - `String coverImage`
        - `EventType type` (Enum: `CACO`, `IC`, `FERIADO`)
        - `EventImportance importance` (Enum: `MAJOR`, `MINOR`)
            - *Regra:* `MAJOR` = Tem página própria. `MINOR` = Só modal.
        - `EventStatus status` (Enum: `SCHEDULED`, `HAPPENING`, `ENDED`)
    */

    public enum EventType {
        CACO,
        IC,
        FERIADO
    }

    public enum EventImportance {
        MAJOR,
        MINOR
    }

    public enum EventStatus {
        SCHEDULED,
        HAPPENING,
        ENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(unique = true, nullable = false)
    private String slug;

    @Lob
    private String description;

    private java.time.LocalDateTime startDate;
    private java.time.LocalDateTime endDate;
    private String location;
    @Column(length = 1000)
    private String locationUrl; // URL para o mapa da localização
    private String coverImage;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private EventImportance importance;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    // Adicionar relacionamento com galeria
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<EventGalleryItem> galleryItems = new ArrayList<>();

    // Método helper para obter URLs das imagens da galeria
    public List<String> getGalleryImageUrls() {
        return galleryItems.stream()
                .filter(item -> item.getType() == EventGalleryItem.MediaType.IMAGE)
                .map(EventGalleryItem::getMediaUrl)
                .toList();
    }
}