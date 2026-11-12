package com.caco.sitedocaco.features.cacomanagement.repository;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CacoManagementRepository extends JpaRepository<CacoManagement, UUID> {

    Page<CacoManagement> findAllByOrderByStartDateDesc(Pageable pageable);
}