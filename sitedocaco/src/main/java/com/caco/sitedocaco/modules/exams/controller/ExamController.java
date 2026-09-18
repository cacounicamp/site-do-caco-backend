package com.caco.sitedocaco.modules.exams.controller;

import com.caco.sitedocaco.modules.exams.dto.response.ExamWithoutSubjectDTO;
import com.caco.sitedocaco.modules.exams.entity.Exam;
import com.caco.sitedocaco.modules.exams.entity.Subject;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.exams.service.ExamService;
import com.caco.sitedocaco.modules.exams.service.SubjectService;
import lombok.RequiredArgsConstructor;
import com.caco.sitedocaco.modules.exams.service.ProfessorService;
import com.caco.sitedocaco.modules.exams.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/exams")
@RequiredArgsConstructor
@RateLimit(capacity = 30, refillTokens = 30, refillPeriod = 1)
public class ExamController {

    private final SubjectService subjectService;
    private final ExamService examService;
    private final ProfessorService professorService;

    @GetMapping("/subjects")
    public ResponseEntity<Page<Subject>> getAllSubjects(@PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Subject> subjects = subjectService.getAllSubjects(pageable);
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/all")
    public ResponseEntity<List<Subject>> getAllSubjectsWithoutPagination() {
        List<Subject> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping
    public ResponseEntity<Page<Exam>> getAllExams(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) UUID professorId,
            @RequestParam(required = false) String subjectCode,
            @PageableDefault(size = 20, sort = "year") Pageable pageable) {
        Page<Exam> exams = examService.getAllExams(year, professorId, subjectCode, pageable);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/subject/{subjectCode}")
    public ResponseEntity<Page<ExamWithoutSubjectDTO>> getExamsBySubject(
            @PathVariable String subjectCode,
            @PageableDefault(size = 20, sort = "year") Pageable pageable) {
        Page<ExamWithoutSubjectDTO> exams = examService.getExamsBySubject(subjectCode, pageable);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAllAvailableYears() {
        List<Integer> years = examService.getAllAvailableYears();
        return ResponseEntity.ok(years);
    }

    @GetMapping("/professors")
    public ResponseEntity<Page<Professor>> getAllProfessors(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Professor> professors = (name == null || name.isBlank())
                ? professorService.getAllProfessors(pageable)
                : professorService.searchProfessorsByName(name, pageable);
        return ResponseEntity.ok(professors);
    }
}