package com.caco.sitedocaco.modules.home.service;

import com.caco.sitedocaco.modules.home.dto.request.CreateWarningDTO;
import com.caco.sitedocaco.modules.home.dto.request.UpdateWarningDTO;
import com.caco.sitedocaco.modules.home.dto.response.WarningDTO;
import com.caco.sitedocaco.modules.home.entity.Warning;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.modules.home.repository.WarningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarningService {

    private final WarningRepository warningRepository;

    @Transactional(readOnly = true)
    public List<WarningDTO> getActiveWarnings() {
        return warningRepository.findActiveWarnings(LocalDateTime.now())
                .stream()
                .map(w -> new WarningDTO(w.getId(), w.getMarkdownText(), w.getSeverityLevel(), w.getStartsAt(), w.getExpiresAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarningDTO> getAllWarnings() {
        return warningRepository.findAll()
                .stream()
                .map(w -> new WarningDTO(w.getId(), w.getMarkdownText(), w.getSeverityLevel(), w.getStartsAt(), w.getExpiresAt()))
                .toList();
    }


    @Transactional
    public Warning createWarning(CreateWarningDTO dto) {
        if (dto.expiresAt().isBefore(dto.startsAt())) {
            throw new IllegalArgumentException("A data de expiração deve ser posterior ao início.");
        }
        Warning warning = new Warning();
        warning.setMarkdownText(dto.markdownText());
        warning.setSeverityLevel(dto.severityLevel());
        warning.setStartsAt(dto.startsAt());
        warning.setExpiresAt(dto.expiresAt());
        return warningRepository.save(warning);
    }

    /**
     * Função Especial: Expirar Aviso Imediatamente
     * Define o expiresAt para agora, removendo-o da visualização pública
     * (considerando a query de filtro padrão NOW() between start and expires).
     */
    @Transactional
    public void expireWarningNow(UUID id) {
        Warning warning = warningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aviso não encontrado"));

        warning.setExpiresAt(LocalDateTime.now());
        warningRepository.save(warning);
    }

    @Transactional
    public Warning updateWarning(UUID id, UpdateWarningDTO dto) {
        Warning warning = warningRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aviso não encontrado"));

        // Se datas forem passadas, valida consistência
        LocalDateTime newStart = dto.startsAt() != null ? dto.startsAt() : warning.getStartsAt();
        LocalDateTime newEnd = dto.expiresAt() != null ? dto.expiresAt() : warning.getExpiresAt();

        if (dto.startsAt() != null || dto.expiresAt() != null) {
            validateDates(newStart, newEnd);
        }

        if (dto.markdownText() != null) warning.setMarkdownText(dto.markdownText());
        if (dto.severityLevel() != null) warning.setSeverityLevel(dto.severityLevel());
        if (dto.startsAt() != null) warning.setStartsAt(dto.startsAt());
        if (dto.expiresAt() != null) warning.setExpiresAt(dto.expiresAt());

        return warningRepository.save(warning);
    }

    @Transactional
    public void deleteWarning(UUID id) {
        warningRepository.deleteById(id);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BusinessRuleException("Data de expiração deve ser após o início.");
        }
    }
}