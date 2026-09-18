package com.caco.sitedocaco.modules.exams.repository;

import com.caco.sitedocaco.modules.exams.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    boolean existsByName(String name);

    @Query("SELECT p FROM Professor p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Professor> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}

