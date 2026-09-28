package com.caco.sitedocaco.features.forms.controller;

import com.caco.sitedocaco.features.forms.dto.request.SubmitFormRequestDTO;
import com.caco.sitedocaco.features.forms.dto.response.AnswerDTO;
import com.caco.sitedocaco.features.forms.dto.response.MySubmissionDTO;
import com.caco.sitedocaco.features.forms.service.FormSubmissionService;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/forms/{slug}")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RateLimit(capacity = 30, refillTokens = 30)
public class FormSubmissionController {

    private final FormSubmissionService submissionService;

    // ── modo de resposta única (allowMultipleSubmissions=false) ─────────────────

    /** Respostas do usuário logado. Sem envio prévio: submitted=false e answers vazio. */
    @GetMapping("/submission")
    public ResponseEntity<MySubmissionDTO> getMySubmission(@PathVariable String slug) {
        return ResponseEntity.ok(submissionService.getMySubmission(slug));
    }

    /** Envia (ou reenvia, se o formulário permitir edição) o estado completo das respostas. */
    @RateLimit(capacity = 10, refillTokens = 10)
    @PutMapping("/submission")
    public ResponseEntity<MySubmissionDTO> submit(
            @PathVariable String slug,
            @Valid @RequestBody SubmitFormRequestDTO request) {
        return ResponseEntity.ok(submissionService.submit(slug, request));
    }

    // Upload consome bandwidth do storage: limite conservador
    @RateLimit(capacity = 10, refillTokens = 10)
    @PostMapping(value = "/files/{questionCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerDTO> uploadFile(
            @PathVariable String slug,
            @PathVariable String questionCode,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(submissionService.uploadFile(slug, questionCode, file));
    }

    @DeleteMapping("/files/{questionCode}")
    public ResponseEntity<Void> removeFile(@PathVariable String slug, @PathVariable String questionCode) {
        submissionService.removeFile(slug, questionCode);
        return ResponseEntity.noContent().build();
    }

    // ── modo de múltiplas respostas (allowMultipleSubmissions=true) ─────────────

    @GetMapping("/submissions")
    public ResponseEntity<List<MySubmissionDTO>> listSubmissions(@PathVariable String slug) {
        return ResponseEntity.ok(submissionService.listMySubmissions(slug));
    }

    /** Cria uma entrada em branco; use PUT /submissions/{id} para preenchê-la (e /files antes, se houver anexo). */
    @RateLimit(capacity = 10, refillTokens = 10)
    @PostMapping("/submissions")
    public ResponseEntity<MySubmissionDTO> createSubmission(@PathVariable String slug) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.createSubmissionEntry(slug));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<MySubmissionDTO> getSubmission(@PathVariable String slug, @PathVariable UUID submissionId) {
        return ResponseEntity.ok(submissionService.getMySubmissionEntry(slug, submissionId));
    }

    @RateLimit(capacity = 10, refillTokens = 10)
    @PutMapping("/submissions/{submissionId}")
    public ResponseEntity<MySubmissionDTO> updateSubmission(
            @PathVariable String slug,
            @PathVariable UUID submissionId,
            @Valid @RequestBody SubmitFormRequestDTO request) {
        return ResponseEntity.ok(submissionService.updateSubmissionEntry(slug, submissionId, request));
    }

    @DeleteMapping("/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable String slug, @PathVariable UUID submissionId) {
        submissionService.deleteSubmissionEntry(slug, submissionId);
        return ResponseEntity.noContent().build();
    }

    @RateLimit(capacity = 10, refillTokens = 10)
    @PostMapping(value = "/submissions/{submissionId}/files/{questionCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerDTO> uploadFileToEntry(
            @PathVariable String slug,
            @PathVariable UUID submissionId,
            @PathVariable String questionCode,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(submissionService.uploadFileToEntry(slug, submissionId, questionCode, file));
    }

    @DeleteMapping("/submissions/{submissionId}/files/{questionCode}")
    public ResponseEntity<Void> removeFileFromEntry(
            @PathVariable String slug,
            @PathVariable UUID submissionId,
            @PathVariable String questionCode) {
        submissionService.removeFileFromEntry(slug, submissionId, questionCode);
        return ResponseEntity.noContent().build();
    }
}
