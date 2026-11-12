package com.caco.sitedocaco.features.cacomanagement.service;

import com.caco.sitedocaco.features.cacomanagement.dto.request.CreateCacoManagementRoleDTO;
import com.caco.sitedocaco.features.cacomanagement.dto.request.UpdateCacoManagementRoleDTO;
import com.caco.sitedocaco.features.cacomanagement.dto.response.CacoManagementRoleResponseDTO;
import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagementRole;
import com.caco.sitedocaco.features.cacomanagement.repository.CacoManagementRoleRepository;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CacoManagementRoleService {

    private final CacoManagementRoleRepository repository;

    @Transactional(readOnly = true)
    public List<CacoManagementRoleResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(CacoManagementRoleResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public CacoManagementRoleResponseDTO create(CreateCacoManagementRoleDTO dto) {
        if (repository.existsByRoleNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Já existe um cargo com esse nome.");
        }
        CacoManagementRole role = new CacoManagementRole();
        role.setRoleName(dto.name());
        return CacoManagementRoleResponseDTO.fromEntity(repository.save(role));
    }

    @Transactional
    public CacoManagementRoleResponseDTO update(UUID id, UpdateCacoManagementRoleDTO dto) {
        CacoManagementRole role = findById(id);
        if (dto.name() != null && !dto.name().isBlank()
                && !dto.name().equalsIgnoreCase(role.getRoleName())
                && repository.existsByRoleNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Já existe um cargo com esse nome.");
        }
        if (dto.name() != null && !dto.name().isBlank()) role.setRoleName(dto.name());
        return CacoManagementRoleResponseDTO.fromEntity(repository.save(role));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findById(id));
    }

    public CacoManagementRole findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado."));
    }
}
