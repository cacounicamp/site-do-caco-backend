package com.caco.sitedocaco.modules.events.repository;

import com.caco.sitedocaco.modules.events.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findBySlug(String slug);

    // Eventos futuros e em andamento (ordenados por mais próximo)
    @Query("SELECT e FROM Event e WHERE e.endDate >= :now ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Eventos passados (ordenados por mais recente)
    @Query("SELECT e FROM Event e WHERE e.endDate < :now ORDER BY e.startDate DESC")
    Page<Event> findPastEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Eventos dentro de um intervalo de datas com margem
    @Query("SELECT e FROM Event e WHERE " +
            "(e.startDate BETWEEN :startDate AND :endDate) OR " +
            "(e.endDate BETWEEN :startDate AND :endDate) OR " +
            "(e.startDate <= :startDate AND e.endDate >= :endDate) " +
            "ORDER BY e.startDate ASC")
    Page<Event> findEventsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}