package com.caco.sitedocaco.features.cacomanagement.service;

import com.caco.sitedocaco.features.cacomanagement.entity.CacoManagement;
import com.caco.sitedocaco.features.cacomanagement.dto.request.UpdateCacoManagementDTO;
import com.caco.sitedocaco.features.cacomanagement.repository.CacoManagementRepository;
import com.caco.sitedocaco.features.cacomanagement.dto.response.CacoManagementResponseDTO;
import com.caco.sitedocaco.features.cacomanagement.dto.request.CreateCacoManagementDTO;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CacoManagementService {

    private final CacoManagementRepository repository;

    @Transactional(readOnly = true)
    public Page<CacoManagementResponseDTO> findAll(Pageable pageable) {
        return repository.findAllByOrderByStartDateDesc(pageable)
                .map(CacoManagementResponseDTO::fromEntity);
    }


    @Transactional
    public CacoManagementResponseDTO createManagement(CreateCacoManagementDTO dto) {
        CacoManagement cacoManagement = new CacoManagement();

        cacoManagement.setName(dto.name());
        cacoManagement.setStartDate(dto.startDate());
        cacoManagement.setEndDate(dto.endDate());

        return CacoManagementResponseDTO.fromEntity(repository.save(cacoManagement));
    }

    @Transactional
    public CacoManagementResponseDTO updateManagement(UUID id, UpdateCacoManagementDTO dto) {
        CacoManagement management = findById(id);
        if (dto.name() != null && !dto.name().isBlank()) management.setName(dto.name());
        if (dto.startDate() != null) management.setStartDate(dto.startDate());
        if (dto.endDate() != null) management.setEndDate(dto.endDate());
        return CacoManagementResponseDTO.fromEntity(repository.save(management));
    }

    @Transactional
    public void deleteManagement(UUID id) {
        repository.delete(findById(id));
    }

    public CacoManagement findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gestão não encontrada."));
    }
}