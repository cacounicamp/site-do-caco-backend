package com.caco.sitedocaco.modules.stickers.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table
public class RedemptionCode {
    /*
    2. **`RedemptionCode`**
    - `String code` (PK, String aleatória de 8-12 chars)
    - `Sticker sticker` (ManyToOne)
    - `Boolean isOneTimeUse`
    - `Boolean isUsed`
    - `LocalDateTime expiresAt` (Nullable)
    - `LocalDateTime createdAt`
    - `LocalDateTime usedAt` (Nullable)
    */

    @Id
    @Column(length = 12)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(nullable = false)
    private Boolean isOneTimeUse;

    @Column(nullable = false)
    private Boolean isUsed;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isOneTimeUse == null) isOneTimeUse = true;
        if (isUsed == null) isUsed = false;
    }
}
