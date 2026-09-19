package com.caco.sitedocaco.features.exams.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Subject {
    /*
    2. `Subject`
        - `String subjectCode` (Código da disciplina, ex: "MC102", "F159", etc) [PK]
        - `String name`
    */

    @Id
    @Column(nullable = false, unique = true)
    private String subjectCode;

    @Column(nullable = false)
    private String name;
}
