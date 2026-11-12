package com.caco.sitedocaco.features.cacomanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "caco_management")
@Data
public class CacoManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column
    private LocalDate endDate;

    @OneToMany(mappedBy = "cacoManagement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CacoManagementMember> members;
 
}