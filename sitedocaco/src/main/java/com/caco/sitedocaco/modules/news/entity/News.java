package com.caco.sitedocaco.modules.news.entity;

import com.caco.sitedocaco.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {
    /*
    3. **`News`**
        - `UUID id`
        - `String title`
        - `String slug` (Unique, indexado)
        - `String summary` (Max 255 chars)
        - `String content` (TEXT/CLOB - Markdown)
        - `String coverImage`
        - `User author` (ManyToOne)
        - `LocalDateTime publishDate`
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 255)
    private String summary;

    @Lob
    private String content;

    private String coverImage;

    @ManyToOne
    private User author;

    private LocalDateTime publishDate;
}
