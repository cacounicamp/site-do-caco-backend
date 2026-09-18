package com.caco.sitedocaco.modules.users.entity;

import com.caco.sitedocaco.shared.entity.Role;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table
public class User {
    /*
    * 1. **`User`**
        - `Long id` (PK, Identity)
        - `String email` (Unique, Not Null)
        - `String username` (Not Null)
        - `String avatarUrl`
        - `Role role` (Enum: `STUDENT`, `ADMIN`, `EDITOR`)
        - `LocalDateTime createdAt`
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Role role = Role.STUDENT;

    @Column(nullable = false)
    private boolean suspended = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
