package com.caco.sitedocaco.features.cacomanagement.repository;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagement;
import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CacoManagementMemberRepository extends JpaRepository<CacoManagementMember, UUID> {

    List<CacoManagementMember> findByCacoManagementOrderByMemberNameAsc(CacoManagement cacoManagement);

    List<CacoManagementMember> findByCacoManagementIdOrderByMemberNameAsc(UUID cacoManagementId);

    boolean existsByCacoManagementIdAndMemberNameIgnoreCase(UUID cacoManagementId, String memberName);
}
