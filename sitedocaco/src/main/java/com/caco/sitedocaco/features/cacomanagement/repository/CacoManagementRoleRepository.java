package com.caco.sitedocaco.features.cacomanagement.repository;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CacoManagementRoleRepository extends JpaRepository<CacoManagementRole, UUID> {

    Optional<CacoManagementRole> findByRoleNameIgnoreCase(String roleName);

    boolean existsByRoleNameIgnoreCase(String roleName);
}
