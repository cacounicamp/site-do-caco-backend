package com.caco.sitedocaco.modules.home.repository;

import com.caco.sitedocaco.modules.home.entity.Warning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WarningRepository extends JpaRepository<Warning, UUID> {
    // Ordenando por severidade (CRITICAL > HIGH > MEDIUM > LOW) e depois por data de início (mais recente primeiro)
    @Query("SELECT w FROM Warning w WHERE w.startsAt <= :now AND w.expiresAt > :now " +
            "ORDER BY w.severityLevel DESC, w.startsAt DESC")
    List<Warning> findActiveWarnings(LocalDateTime now);
}