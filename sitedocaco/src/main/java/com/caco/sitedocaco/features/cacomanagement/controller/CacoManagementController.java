package com.caco.sitedocaco.features.cacomanagement.controller;

import com.caco.sitedocaco.features.cacomanagement.dto.request.*;
import com.caco.sitedocaco.features.cacomanagement.dto.response.*;
import com.caco.sitedocaco.features.cacomanagement.service.CacoManagementMemberService;
import com.caco.sitedocaco.features.cacomanagement.service.CacoManagementRoleService;
import com.caco.sitedocaco.features.cacomanagement.service.CacoManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/caco-management")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class CacoManagementController {

    private final CacoManagementService managementService;
    private final CacoManagementMemberService memberService;
    private final CacoManagementRoleService roleService;

    @GetMapping
    public ResponseEntity<Page<CacoManagementResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(managementService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<CacoManagementResponseDTO> create(@RequestBody @Valid CreateCacoManagementDTO dto) {
        return ResponseEntity.ok(managementService.createManagement(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CacoManagementResponseDTO> update(
            @PathVariable UUID id, @RequestBody @Valid UpdateCacoManagementDTO dto) {
        return ResponseEntity.ok(managementService.updateManagement(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        managementService.deleteManagement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{managementId}/members")
    public ResponseEntity<List<CacoManagementMemberResponseDTO>> findMembers(@PathVariable UUID managementId) {
        return ResponseEntity.ok(memberService.findByManagement(managementId));
    }

    @PostMapping("/{managementId}/members")
    public ResponseEntity<CacoManagementMemberResponseDTO> createMember(
            @PathVariable UUID managementId,
            @RequestBody @Valid CreateCacoManagementMemberDTO dto) {
        return ResponseEntity.ok(memberService.create(dto, managementId));
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<CacoManagementMemberResponseDTO> updateMember(
            @PathVariable UUID id, @RequestBody @Valid UpdateCacoManagementMemberDTO dto) {
        return ResponseEntity.ok(memberService.update(id, dto));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<CacoManagementRoleResponseDTO>> findRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping("/roles")
    public ResponseEntity<CacoManagementRoleResponseDTO> createRole(
            @RequestBody @Valid CreateCacoManagementRoleDTO dto) {
        return ResponseEntity.ok(roleService.create(dto));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<CacoManagementRoleResponseDTO> updateRole(
            @PathVariable UUID id, @RequestBody @Valid UpdateCacoManagementRoleDTO dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}