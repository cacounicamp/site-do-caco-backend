package com.caco.sitedocaco.modules.whatsapp.repository;

import com.caco.sitedocaco.modules.whatsapp.entity.WhatsAppGroup;
import com.caco.sitedocaco.shared.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhatsAppGroupRepository extends JpaRepository<WhatsAppGroup, UUID> {
    Optional<WhatsAppGroup> findByCourseAndEntryYear(CourseType course, int entryYear);
    boolean existsByCourseAndEntryYear(CourseType course, int entryYear);
}

