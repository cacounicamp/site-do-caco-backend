package com.caco.sitedocaco.modules.events.repository;

import com.caco.sitedocaco.modules.users.entity.User;
import com.caco.sitedocaco.modules.events.entity.Event;
import com.caco.sitedocaco.modules.events.entity.UserEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, UUID> {

    Optional<UserEvent> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    // Páginação com ordenação por savedAt decrescente (mais recente primeiro)
    @Query("SELECT ue FROM UserEvent ue WHERE ue.user = :user AND ue.status != 'NOT_GOING' ORDER BY ue.savedAt DESC")
    Page<UserEvent> findByUserOrderBySavedAtDesc(@Param("user") User user, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM user_event WHERE event_id = :eventId AND status = :status", nativeQuery = true)
    long countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") String status);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_event WHERE user_id = :userId AND event_id = :eventId)", nativeQuery = true)
    boolean existsByUserIdAndEventId(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    @Query(value = "SELECT status FROM user_event WHERE user_id = :userId AND event_id = :eventId", nativeQuery = true)
    Optional<String> findStatusByUserIdAndEventId(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    void deleteByUserAndEvent(User user, Event event);

    @Query(value = "DELETE FROM user_event WHERE event_id = :eventId", nativeQuery = true)
    void deleteAllByEventId(@Param("eventId") UUID eventId);
}