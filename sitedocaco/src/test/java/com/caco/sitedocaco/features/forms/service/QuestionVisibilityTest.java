package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionVisibilityTest {

    private final FormQuestion parent = question();
    private final FormOption yes = option();
    private final FormOption no = option();

    private static FormQuestion question() {
        FormQuestion q = new FormQuestion();
        q.setId(UUID.randomUUID());
        return q;
    }

    private static FormOption option() {
        FormOption o = new FormOption();
        o.setId(UUID.randomUUID());
        return o;
    }

    private FormQuestion child(FormOption trigger) {
        FormQuestion child = question();
        child.setShowIfQuestion(parent);
        child.setShowIfOption(trigger);
        return child;
    }

    @Test
    void questionWithoutConditionIsAlwaysVisible() {
        assertThat(QuestionVisibility.isVisible(question(), Set.of(), Map.of())).isTrue();
    }

    @Test
    void visibleOnlyWhenTriggerOptionWasPicked() {
        FormQuestion child = child(yes);
        Set<UUID> visible = Set.of(parent.getId());

        assertThat(QuestionVisibility.isVisible(child, visible, Map.of(parent.getId(), Set.of(yes.getId())))).isTrue();
        assertThat(QuestionVisibility.isVisible(child, visible, Map.of(parent.getId(), Set.of(no.getId())))).isFalse();
        assertThat(QuestionVisibility.isVisible(child, visible, Map.of())).isFalse();
    }

    @Test
    void withoutTriggerAnyPickedOptionShowsIt() {
        FormQuestion child = child(null);
        Set<UUID> visible = Set.of(parent.getId());

        assertThat(QuestionVisibility.isVisible(child, visible, Map.of(parent.getId(), Set.of(no.getId())))).isTrue();
        assertThat(QuestionVisibility.isVisible(child, visible, Map.of(parent.getId(), Set.of()))).isFalse();
    }

    @Test
    void hiddenParentHidesChildEvenIfItsStaleSelectionIsPresent() {
        FormQuestion child = child(yes);

        assertThat(QuestionVisibility.isVisible(child, new HashSet<>(), Map.of(parent.getId(), Set.of(yes.getId())))).isFalse();
    }
}
