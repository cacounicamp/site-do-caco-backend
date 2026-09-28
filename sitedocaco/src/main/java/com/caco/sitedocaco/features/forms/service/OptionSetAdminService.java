package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.CreateOptionSetDTO;
import com.caco.sitedocaco.features.forms.dto.request.OptionInputDTO;
import com.caco.sitedocaco.features.forms.dto.request.UpdateOptionSetDTO;
import com.caco.sitedocaco.features.forms.dto.response.OptionSetDTO;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.OptionSet;
import com.caco.sitedocaco.features.forms.repository.FormAnswerOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormOptionRepository;
import com.caco.sitedocaco.features.forms.repository.FormQuestionRepository;
import com.caco.sitedocaco.features.forms.repository.OptionSetRepository;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionSetAdminService {

    private final OptionSetRepository optionSetRepository;
    private final FormOptionRepository optionRepository;
    private final FormQuestionRepository questionRepository;
    private final FormAnswerOptionRepository answerOptionRepository;

    @Transactional(readOnly = true)
    public List<OptionSetDTO> list() {
        List<OptionSet> sets = optionSetRepository.findAllByOrderBySlugAsc();
        if (sets.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<FormOption>> optionsBySet = optionRepository
                .findByOptionSetIdInOrderByDisplayOrderAscIdAsc(sets.stream().map(OptionSet::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(o -> o.getOptionSet().getId()));

        return sets.stream()
                .map(set -> OptionSetDTO.from(set, optionsBySet.getOrDefault(set.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public OptionSetDTO get(UUID id) {
        return toDTO(getSet(id));
    }

    @Transactional
    public OptionSetDTO create(CreateOptionSetDTO dto) {
        if (optionSetRepository.existsBySlug(dto.slug())) {
            throw new BusinessRuleException("Já existe um conjunto de opções com esse slug.");
        }

        OptionSet set = new OptionSet();
        set.setSlug(dto.slug());
        set.setName(dto.name().strip());
        optionSetRepository.save(set);

        syncOptions(set, dto.options());
        return toDTO(set);
    }

    @Transactional
    public OptionSetDTO update(UUID id, UpdateOptionSetDTO dto) {
        OptionSet set = getSet(id);
        set.setName(dto.name().strip());
        optionSetRepository.save(set);

        syncOptions(set, dto.options());
        return toDTO(set);
    }

    @Transactional
    public void delete(UUID id) {
        OptionSet set = getSet(id);
        if (questionRepository.existsByOptionSet(set)) {
            throw new BusinessRuleException("Este conjunto de opções é usado por perguntas e não pode ser excluído.");
        }

        optionRepository.deleteAll(optionRepository.findByOptionSetIdInOrderByDisplayOrderAscIdAsc(List.of(set.getId())));
        optionSetRepository.delete(set);
    }

    /**
     * Casa as opções pelo código. Uma opção que sai da lista, ou é desativada, não pode ser gatilho de
     * condicional de pergunta ativa; se já foi usada em respostas (ou em condicional inativa) fica só
     * desativada, senão é excluída.
     */
    private void syncOptions(OptionSet set, List<OptionInputDTO> inputs) {
        Set<String> codes = new HashSet<>();
        for (OptionInputDTO input : inputs) {
            if (!codes.add(input.code())) {
                throw new BusinessRuleException("Código de opção repetido: '" + input.code() + "'.");
            }
        }

        Map<String, FormOption> existing = optionRepository
                .findByOptionSetIdInOrderByDisplayOrderAscIdAsc(List.of(set.getId())).stream()
                .collect(Collectors.toMap(FormOption::getCode, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        int order = 0;
        for (OptionInputDTO input : inputs) {
            boolean active = input.active() == null || input.active();
            FormOption option = existing.get(input.code());

            if (option == null) {
                option = new FormOption();
                option.setOptionSet(set);
                option.setCode(input.code());
            } else if (option.isActive() && !active) {
                assertNotActiveTrigger(option);
            }

            option.setLabel(input.label().strip());
            option.setAllowsFreeText(Boolean.TRUE.equals(input.allowsFreeText()));
            option.setActive(active);
            option.setDisplayOrder(order++);
            optionRepository.save(option);
        }

        for (FormOption removed : existing.values()) {
            if (codes.contains(removed.getCode())) {
                continue;
            }
            assertNotActiveTrigger(removed);

            if (answerOptionRepository.existsByOption(removed) || questionRepository.existsByShowIfOption(removed)) {
                removed.setActive(false);
                removed.setDisplayOrder(order++);
                optionRepository.save(removed);
            } else {
                optionRepository.delete(removed);
            }
        }
    }

    private void assertNotActiveTrigger(FormOption option) {
        if (questionRepository.existsByShowIfOptionAndActiveTrue(option)) {
            throw new BusinessRuleException("A opção '" + option.getCode()
                    + "' é gatilho de condicional de uma pergunta ativa: ajuste a pergunta antes de removê-la ou desativá-la.");
        }
    }

    private OptionSet getSet(UUID id) {
        return optionSetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conjunto de opções não encontrado."));
    }

    private OptionSetDTO toDTO(OptionSet set) {
        return OptionSetDTO.from(set, optionRepository.findByOptionSetIdInOrderByDisplayOrderAscIdAsc(List.of(set.getId())));
    }
}
