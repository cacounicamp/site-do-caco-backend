package com.caco.sitedocaco.features.cacomanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;



@Entity
@Table(name = "caco_management_member")
@Data
public class CacoManagementMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "caco_management_id", nullable = false)
    private CacoManagement cacoManagement;

    @Column(nullable = false)
    private String memberName;

    @ManyToOne
    @JoinColumn(name = "member_role_id", nullable = false)
    private CacoManagementRole memberRole;
}