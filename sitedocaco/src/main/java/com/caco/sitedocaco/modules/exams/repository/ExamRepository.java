// main/java/com.caco.sitedocaco.modules.exams.repository.ExamRepository.java
package com.caco.sitedocaco.modules.exams.repository;

import com.caco.sitedocaco.modules.exams.entity.ExamType;
import com.caco.sitedocaco.modules.exams.entity.Exam;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    List<Exam> findBySubjectSubjectCode(String subjectCode);
    List<Exam> findByYear(Integer year);
    List<Exam> findByType(ExamType type);
    List<Exam> findBySubject(Subject subject);
    Page<Exam> findBySubject(Subject subject, Pageable pageable);

    @Query("""
            SELECT e
            FROM Exam e
            WHERE (:year IS NULL OR e.year = :year)
              AND (:professorId IS NULL OR e.professor.id = :professorId)
              AND (:subjectCode IS NULL OR UPPER(e.subject.subjectCode) = UPPER(:subjectCode))
            """)
    Page<Exam> findAllWithFilters(
            @Param("year") Integer year,
            @Param("professorId") UUID professorId,
            @Param("subjectCode") String subjectCode,
            Pageable pageable
    );

    @Query("SELECT DISTINCT e.year FROM Exam e WHERE e.year IS NOT NULL ORDER BY e.year DESC")
    List<Integer> findAllDistinctYears();

    @Transactional
    @Modifying
    @Query("DELETE FROM Exam e WHERE e.subject.subjectCode = :subjectCode")
    void deleteBySubjectSubjectCode(@Param("subjectCode") String subjectCode);

    @Transactional
    @Modifying
    @Query("UPDATE Exam e SET e.professor = null WHERE e.professor.id = :professorId")
    void removeProfessorFromExams(@Param("professorId") UUID professorId);
}