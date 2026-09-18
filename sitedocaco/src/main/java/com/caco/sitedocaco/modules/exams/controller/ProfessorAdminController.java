package com.caco.sitedocaco.modules.exams.controller;

import com.caco.sitedocaco.modules.exams.dto.request.CreateProfessorDTO;
import com.caco.sitedocaco.modules.exams.dto.request.UpdateProfessorDTO;
import com.caco.sitedocaco.modules.exams.entity.Professor;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.exams.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/professors")
@RequiredArgsConstructor
@RateLimit(capacity = 30, refillTokens = 30)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProfessorAdminController {

    private final ProfessorService professorService;

    @PostMapping
    public ResponseEntity<Professor> createProfessor(@RequestBody @Valid CreateProfessorDTO dto) {
        Professor professor = professorService.createProfessor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(professor);
    }

    @GetMapping
    public ResponseEntity<List<Professor>> getAllProfessors() {
        List<Professor> professors = professorService.getAllProfessors();
        return ResponseEntity.ok(professors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> getProfessor(@PathVariable UUID id) {
        Professor professor = professorService.getProfessorById(id);
        return ResponseEntity.ok(professor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> updateProfessor(
            @PathVariable UUID id,
            @RequestBody UpdateProfessorDTO dto) {
        Professor professor = professorService.updateProfessor(id, dto);
        return ResponseEntity.ok(professor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable UUID id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}

