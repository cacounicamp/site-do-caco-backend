package com.caco.sitedocaco.features.forms.controller;

import com.caco.sitedocaco.features.forms.dto.request.CreateOptionSetDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateOptionSetDTO;
import com.caco.sitedocaco.features.forms.dto.response.OptionSetDTO;
import com.caco.sitedocaco.features.forms.service.OptionSetAdminService;
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
@RequestMapping("/admin/form-option-sets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RateLimit(capacity = 60, refillTokens = 60)
public class OptionSetAdminController {

    private final OptionSetAdminService optionSetAdminService;

    @GetMapping
    public ResponseEntity<List<OptionSetDTO>> list() {
        return ResponseEntity.ok(optionSetAdminService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionSetDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(optionSetAdminService.get(id));
    }

    @PostMapping
    public ResponseEntity<OptionSetDTO> create(@Valid @RequestBody CreateOptionSetDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(optionSetAdminService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OptionSetDTO> update(@PathVariable UUID id, @Valid @RequestBody UpdateOptionSetDTO dto) {
        return ResponseEntity.ok(optionSetAdminService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        optionSetAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
