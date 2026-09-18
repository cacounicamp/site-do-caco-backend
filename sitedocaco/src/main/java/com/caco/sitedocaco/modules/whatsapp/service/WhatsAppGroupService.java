package com.caco.sitedocaco.modules.whatsapp.service;

import com.caco.sitedocaco.modules.whatsapp.dto.request.WhatsAppGroupRequest;
import com.caco.sitedocaco.modules.whatsapp.dto.response.WhatsAppGroupDTO;
import com.caco.sitedocaco.modules.users.entity.UserProfile;
import com.caco.sitedocaco.modules.whatsapp.entity.WhatsAppGroup;
import com.caco.sitedocaco.shared.entity.CourseType;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import com.caco.sitedocaco.shared.contract.UserProfileAccess;
import com.caco.sitedocaco.shared.contract.WhatsAppLinkProvider;
import com.caco.sitedocaco.modules.whatsapp.repository.WhatsAppGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhatsAppGroupService implements WhatsAppLinkProvider {

    private final WhatsAppGroupRepository whatsAppGroupRepository;
    private final UserProfileAccess userProfileAccess;

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WhatsAppGroupDTO> listAll() {
        return whatsAppGroupRepository.findAll().stream()
                .map(WhatsAppGroupDTO::fromEntity)
                .toList();
    }

    @Transactional
    public WhatsAppGroupDTO create(WhatsAppGroupRequest request) {
        if (whatsAppGroupRepository.existsByCourseAndEntryYear(request.course(), request.entryYear())) {
            throw new BusinessRuleException("Já existe um grupo de WhatsApp para este curso e ano.");
        }
        WhatsAppGroup group = new WhatsAppGroup();
        group.setCourse(request.course());
        group.setEntryYear(request.entryYear());
        group.setWhatsappLink(request.whatsappLink());
        return WhatsAppGroupDTO.fromEntity(whatsAppGroupRepository.save(group));
    }

    @Transactional
    public WhatsAppGroupDTO update(UUID id, WhatsAppGroupRequest request) {
        WhatsAppGroup group = whatsAppGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo de WhatsApp não encontrado."));

        // Se mudou curso/ano, verifica unicidade
        boolean courseOrYearChanged = group.getCourse() != request.course()
                || group.getEntryYear() != request.entryYear();
        if (courseOrYearChanged
                && whatsAppGroupRepository.existsByCourseAndEntryYear(request.course(), request.entryYear())) {
            throw new BusinessRuleException("Já existe um grupo de WhatsApp para este curso e ano.");
        }

        group.setCourse(request.course());
        group.setEntryYear(request.entryYear());
        group.setWhatsappLink(request.whatsappLink());
        return WhatsAppGroupDTO.fromEntity(whatsAppGroupRepository.save(group));
    }

    @Transactional
    public void delete(UUID id) {
        if (!whatsAppGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Grupo de WhatsApp não encontrado.");
        }
        whatsAppGroupRepository.deleteById(id);
    }

    // ── Used by UserService (/me) ─────────────────────────────────────────────

    /**
     * Retorna o link do grupo de WhatsApp associado ao curso e ano do usuário logado,
     * ou null se não existir.
     * Somente disponível para cursos reconhecidos (não OUTRO).
     */
    @Transactional(readOnly = true)
    @Override
    public String getWhatsAppLinkForCurrentUser() {
        Optional<UserProfile> profileOpt = userProfileAccess.findMyProfile();
        if (profileOpt.isEmpty()) return null;

        UserProfile profile = profileOpt.get();
        if (profile.getCourse() == CourseType.OUTRO) return null;

        return whatsAppGroupRepository
                .findByCourseAndEntryYear(profile.getCourse(), profile.getEntryYear())
                .map(WhatsAppGroup::getWhatsappLink)
                .orElse(null);
    }
}
