package com.caco.sitedocaco.features.forms.controller;

import com.caco.sitedocaco.features.forms.dto.request.SubmitFormRequestDTO;
import com.caco.sitedocaco.features.forms.dto.response.AnswerDTO;
import com.caco.sitedocaco.features.forms.dto.response.MySubmissionDTO;
import com.caco.sitedocaco.features.forms.service.FormSubmissionService;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user/forms/{slug}")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RateLimit(capacity = 30, refillTokens = 30)
public class FormSubmissionController {

    private final FormSubmissionService submissionService;

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
}
