package com.caco.sitedocaco.modules.stickers.entity;

import com.caco.sitedocaco.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "user_sticker",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_sticker_user_sticker", columnNames = {"user_id", "sticker_id"})
        },
        indexes = {
                @Index(name = "idx_user_sticker_user", columnList = "user_id"),
                @Index(name = "idx_user_sticker_sticker", columnList = "sticker_id")
        }
)
public class UserSticker {
    /*
    1. **`UserSticker`**
        - `UUID id`
        - `User user` (ManyToOne)
        - `Sticker sticker` (ManyToOne)
        - `LocalDateTime obtainedAt`
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(nullable = false)
    private LocalDateTime obtainedAt;

    @PrePersist
    public void prePersist() {
        if (obtainedAt == null) {
            obtainedAt = LocalDateTime.now();
        }
    }
}
