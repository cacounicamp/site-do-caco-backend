package com.caco.sitedocaco.modules.exams.controller;

import com.caco.sitedocaco.modules.exams.dto.request.CreateExamDTO;
import com.caco.sitedocaco.modules.exams.dto.request.CreateSubjectDTO;
import com.caco.sitedocaco.modules.exams.dto.request.UpdateExamDTO;
import com.caco.sitedocaco.modules.exams.dto.response.ExamWithoutSubjectDTO;
import com.caco.sitedocaco.modules.exams.entity.Exam;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.exams.service.ExamService;
import com.caco.sitedocaco.modules.exams.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/exams")
@RequiredArgsConstructor
@RateLimit(capacity = 30, refillTokens = 30)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ExamAdminController {

    private final SubjectService subjectService;
    private final ExamService examService;

    // ==================== DISCIPLINAS ====================

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@RequestBody @Valid CreateSubjectDTO dto) {
        Subject createdSubject = subjectService.createSubject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/{subjectCode}")
    public ResponseEntity<Subject> getSubject(@PathVariable String subjectCode) {
        Subject subject = subjectService.getSubjectByCode(subjectCode);
        return ResponseEntity.ok(subject);
    }

    @DeleteMapping("/subjects/{subjectCode}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String subjectCode) {
        subjectService.deleteSubject(subjectCode);
        return ResponseEntity.noContent().build();
    }

    // ==================== PROVAS ====================

    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody @Valid CreateExamDTO dto) {
        Exam createdExam = examService.createExam(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExam);
    }

    @GetMapping("")
    public ResponseEntity<List<Exam>> getAllExams() {
        List<Exam> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/subject/{subjectCode}")
    public ResponseEntity<List<ExamWithoutSubjectDTO>> getExamsBySubject(@PathVariable String subjectCode) {
        List<ExamWithoutSubjectDTO> exams = examService.getExamsBySubject(subjectCode);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExam(@PathVariable UUID id) {
        Exam exam = examService.getExamById(id);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(
            @PathVariable UUID id,
            @RequestBody UpdateExamDTO dto) {

        Exam updatedExam = examService.updateExam(id, dto);
        return ResponseEntity.ok(updatedExam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable UUID id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
}