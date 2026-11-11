package com.caco.sitedocaco.shared.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table
public class AnalyticsLog {
    /*
    1. **`AnalyticsLog`**
        - `UUID id`
        - `AnalyticsType type` (Enum: `VIEW_ARTICLE`, `VIEW_EVENT`, `SEARCH_QUERY`)
        - `String targetId` (Pode ser ID do artigo ou o termo buscado)
        - `LocalDateTime timestamp`
    */
    public enum AnalyticsType {
        VIEW_ARTICLE,
        VIEW_EVENT,
        SEARCH_QUERY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private AnalyticsType type;

    private String targetId;
}
