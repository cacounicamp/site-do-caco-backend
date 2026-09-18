package com.caco.sitedocaco.modules.manual.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class ManualChapter {
    /*
    2. **`ManualChapter`**
        - `UUID id`
        - `String slug` (unique, URL-friendly)
        - `String title`
        - `Integer order`
        - `ManualCategory category` (ManyToOne)
        - `List<ManualArticle> articles` (OneToMany)
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(name = "chapter_order")
    private Integer order;

    @ManyToOne
    private ManualCategory category;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ManualArticle> articles;
}
