package com.caco.sitedocaco.modules.manual.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table
@Data
public class ManualArticle {
    /*
    3. **`ManualArticle`**
        - `UUID id`
        - `String slug` (unique, URL-friendly)
        - `String title`
        - `String content` (TEXT - Markdown)
        - `Integer order`
        - `ManualChapter chapter` (ManyToOne)
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @Column(name = "article_order")
    private Integer order;

    @ManyToOne
    private ManualChapter chapter;
}
