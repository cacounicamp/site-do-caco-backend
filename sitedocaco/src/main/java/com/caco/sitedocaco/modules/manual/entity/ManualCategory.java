package com.caco.sitedocaco.modules.manual.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
public class ManualCategory {
    /*
    1. **`ManualCategory`**
        - `UUID id`
        - `String slug` (unique, URL-friendly)
        - `String title`
        - `Integer order`
        - `List<ManualChapter> chapters` (OneToMany)
    */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(name = "category_order")
    private Integer order;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ManualChapter> chapters;
}
