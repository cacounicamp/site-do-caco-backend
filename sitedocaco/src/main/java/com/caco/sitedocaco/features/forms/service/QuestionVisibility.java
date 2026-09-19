package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Regra de visibilidade condicional, sem acesso a banco. */
final class QuestionVisibility {

    private QuestionVisibility() {
    }

    /**
     * Uma pergunta é visível se não tem condicional, ou se a pergunta-mãe é ela própria visível
     * e a opção-gatilho está entre as marcadas (qualquer opção, se não há gatilho específico).
     *
     * @param visibleIds ids das perguntas já avaliadas como visíveis (a mãe sempre vem antes da filha)
     * @param selected   opções marcadas por pergunta visível
     */
    static boolean isVisible(FormQuestion question, Set<UUID> visibleIds, Map<UUID, Set<UUID>> selected) {
        FormQuestion parent = question.getShowIfQuestion();
        if (parent == null) {
            return true;
        }
        if (!visibleIds.contains(parent.getId())) {
            return false;
        }

        Set<UUID> picked = selected.getOrDefault(parent.getId(), Set.of());
        FormOption trigger = question.getShowIfOption();
        return trigger == null ? !picked.isEmpty() : picked.contains(trigger.getId());
    }
}
