package com.caco.sitedocaco.features.forms.controller;

import com.caco.sitedocaco.features.forms.dto.request.CreateFormDTO;
import com.caco.sitedocaco.features.forms.dto.request.CreateFormQuestionDTO;
import com.caco.sitedocaco.features.forms.dto.request.ReorderFormQuestionsDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateFormDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateFormQuestionDTO;
import com.caco.sitedocaco.features.forms.dto.response.FormAdminDTO;
import com.caco.sitedocaco.features.forms.dto.response.FormSummaryAdminDTO;
import com.caco.sitedocaco.features.forms.dto.response.QuestionAdminDTO;
import com.caco.sitedocaco.features.forms.service.FormAdminService;
import com.caco.sitedocaco.features.forms.service.FormQuestionAdminService;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/forms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 60, refillTokens = 60)
public class FormAdminController {

    private final FormAdminService formAdminService;
    private final FormQuestionAdminService questionAdminService;

    @GetMapping
    public ResponseEntity<List<FormSummaryAdminDTO>> list() {
        return ResponseEntity.ok(formAdminService.list());
    }

    @GetMapping("/{formId}")
    public ResponseEntity<FormAdminDTO> get(@PathVariable UUID formId) {
        return ResponseEntity.ok(formAdminService.get(formId));
    }

    @PostMapping
    public ResponseEntity<FormAdminDTO> create(@Valid @RequestBody CreateFormDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formAdminService.create(dto));
    }

    @PutMapping("/{formId}")
    public ResponseEntity<FormAdminDTO> update(@PathVariable UUID formId, @Valid @RequestBody UpdateFormDTO dto) {
        return ResponseEntity.ok(formAdminService.update(formId, dto));
    }

    @DeleteMapping("/{formId}")
    public ResponseEntity<Void> delete(@PathVariable UUID formId) {
        formAdminService.delete(formId);
        return ResponseEntity.noContent().build();
    }

    // ── Perguntas ─────────────────────────────────────────────────────────────

    @PostMapping("/{formId}/questions")
    public ResponseEntity<QuestionAdminDTO> createQuestion(
            @PathVariable UUID formId,
            @Valid @RequestBody CreateFormQuestionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionAdminService.create(formId, dto));
    }

    @PutMapping("/{formId}/questions/{questionId}")
    public ResponseEntity<QuestionAdminDTO> updateQuestion(
            @PathVariable UUID formId,
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateFormQuestionDTO dto) {
        return ResponseEntity.ok(questionAdminService.update(formId, questionId, dto));
    }

    @DeleteMapping("/{formId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID formId, @PathVariable UUID questionId) {
        questionAdminService.delete(formId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{formId}/questions/reorder")
    public ResponseEntity<List<QuestionAdminDTO>> reorderQuestions(
            @PathVariable UUID formId,
            @Valid @RequestBody ReorderFormQuestionsDTO dto) {
        return ResponseEntity.ok(questionAdminService.reorder(formId, dto));
    }
}
