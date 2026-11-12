package com.caco.sitedocaco.features.cacomanagement.service;

import com.caco.sitedocaco.features.cacomanagement.dto.request.CreateCacoManagementMemberDTO;
import com.caco.sitedocaco.features.cacomanagement.dto.request.UpdateCacoManagementMemberDTO;
import com.caco.sitedocaco.features.cacomanagement.dto.response.CacoManagementMemberResponseDTO;
import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagement;
import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementMember;
import com.caco.sitedocaco.features.cacomanagement.repository.CacoManagementMemberRepository;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CacoManagementMemberService {

    private final CacoManagementMemberRepository repository;
    private final CacoManagementService managementService;
    private final CacoManagementRoleService roleService;

    @Transactional(readOnly = true)
    public List<CacoManagementMemberResponseDTO> findByManagement(UUID managementId) {
        managementService.findById(managementId);
        return repository.findByCacoManagementIdOrderByMemberNameAsc(managementId).stream()
                .map(CacoManagementMemberResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public CacoManagementMemberResponseDTO create(CreateCacoManagementMemberDTO dto, UUID managementId) {
        if (!managementId.equals(dto.cacoManagementId())) {
            throw new IllegalArgumentException("A gestão informada na URL não corresponde à gestão do corpo da requisição.");
        }
        CacoManagement management = managementService.findById(managementId);
        if (repository.existsByCacoManagementIdAndMemberNameIgnoreCase(managementId, dto.name())) {
            throw new IllegalArgumentException("Já existe um membro com esse nome nesta gestão.");
        }
        CacoManagementMember member = new CacoManagementMember();
        member.setCacoManagement(management);
        member.setMemberName(dto.name());
        member.setMemberRole(roleService.findById(dto.roleId()));
        return CacoManagementMemberResponseDTO.fromEntity(repository.save(member));
    }

    @Transactional
    public CacoManagementMemberResponseDTO update(UUID id, UpdateCacoManagementMemberDTO dto) {
        CacoManagementMember member = findById(id);
        if (dto.name() != null && !dto.name().isBlank()) member.setMemberName(dto.name());
        if (dto.roleId() != null) member.setMemberRole(roleService.findById(dto.roleId()));
        return CacoManagementMemberResponseDTO.fromEntity(repository.save(member));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findById(id));
    }

    public CacoManagementMember findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado."));
    }
}
