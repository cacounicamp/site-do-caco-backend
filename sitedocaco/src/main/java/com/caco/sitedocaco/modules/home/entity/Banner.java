package com.caco.sitedocaco.modules.home.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Table
@Entity
@Data
public class Banner {
    /*
    1. **`Banner`**
        - `UUID id`
        - `String title`
        - `String imageUrl`
        - `String targetLink`
        - `Integer displayOrder`
        - `Boolean active`
    * */

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String imageUrl;
    private String targetLink;
    private Integer displayOrder;
    private Boolean active;
}
