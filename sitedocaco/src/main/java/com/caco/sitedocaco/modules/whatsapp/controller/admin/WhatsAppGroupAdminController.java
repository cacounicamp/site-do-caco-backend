package com.caco.sitedocaco.modules.whatsapp.controller.admin;

import com.caco.sitedocaco.modules.whatsapp.dto.request.WhatsAppGroupRequest;
import com.caco.sitedocaco.modules.whatsapp.dto.response.WhatsAppGroupDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.whatsapp.service.WhatsAppGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/whatsapp-groups")
@RequiredArgsConstructor
@RateLimit(capacity = 30, refillTokens = 30)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class WhatsAppGroupAdminController {

    private final WhatsAppGroupService whatsAppGroupService;

    @GetMapping
    public ResponseEntity<List<WhatsAppGroupDTO>> listAll() {
        return ResponseEntity.ok(whatsAppGroupService.listAll());
    }

    @PostMapping
    public ResponseEntity<WhatsAppGroupDTO> create(@Valid @RequestBody WhatsAppGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(whatsAppGroupService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WhatsAppGroupDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody WhatsAppGroupRequest request) {
        return ResponseEntity.ok(whatsAppGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        whatsAppGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
