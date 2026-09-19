package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.AnswerInputDTO;
import com.caco.sitedocaco.features.forms.dto.request.ChoiceInputDTO;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnswerValidatorTest {

    private static FormQuestion question(QuestionType type) {
        FormQuestion q = new FormQuestion();
        q.setId(UUID.randomUUID());
        q.setCode("q");
        q.setPrompt("Pergunta");
        q.setType(type);
        return q;
    }

    private static FormOption option(String code, boolean freeText) {
        FormOption o = new FormOption();
        o.setId(UUID.randomUUID());
        o.setCode(code);
        o.setLabel(code.toUpperCase());
        o.setAllowsFreeText(freeText);
        return o;
    }

    private static Map<String, FormOption> options(FormOption... options) {
        return java.util.Arrays.stream(options).collect(java.util.stream.Collectors.toMap(FormOption::getCode, o -> o));
    }

    private static AnswerInputDTO choices(ChoiceInputDTO... items) {
        return new AnswerInputDTO("q", null, List.of(items));
    }

    private static AnswerInputDTO text(String value) {
        return new AnswerInputDTO("q", value, null);
    }

    // ── escolha ───────────────────────────────────────────────────────────────

    @Test
    void singleChoiceRejectsMoreThanOneOption() {
        FormOption a = option("a", false), b = option("b", false);

        assertThatThrownBy(() -> AnswerValidator.parse(question(QuestionType.SINGLE_CHOICE),
                choices(new ChoiceInputDTO("a", null), new ChoiceInputDTO("b", null)), options(a, b)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("apenas uma");
    }

    @Test
    void multipleChoiceAcceptsSeveralAndKeepsFreeTextOnlyWhereAllowed() {
        FormOption a = option("a", false), other = option("other", true);

        AnswerValidator.Parsed parsed = AnswerValidator.parse(question(QuestionType.MULTIPLE_CHOICE),
                choices(new ChoiceInputDTO("a", "ignorado"), new ChoiceInputDTO("other", "  minha resposta ")),
                options(a, other));

        assertThat(parsed.choices()).hasSize(2);
        assertThat(parsed.choices().get(0).freeText()).isNull();
        assertThat(parsed.choices().get(1).freeText()).isEqualTo("minha resposta");
    }

    @Test
    void choiceRejectsUnknownOptionDuplicateAndMissingFreeText() {
        FormOption a = option("a", false), other = option("other", true);
        FormQuestion q = question(QuestionType.MULTIPLE_CHOICE);

        assertThatThrownBy(() -> AnswerValidator.parse(q, choices(new ChoiceInputDTO("zzz", null)), options(a, other)))
                .hasMessageContaining("opção inválida");
        assertThatThrownBy(() -> AnswerValidator.parse(q,
                choices(new ChoiceInputDTO("a", null), new ChoiceInputDTO("a", null)), options(a, other)))
                .hasMessageContaining("repetida");
        assertThatThrownBy(() -> AnswerValidator.parse(q, choices(new ChoiceInputDTO("other", "  ")), options(a, other)))
                .hasMessageContaining("especifique");
    }

    @Test
    void choiceWithoutInputIsEmpty() {
        assertThat(AnswerValidator.parse(question(QuestionType.SINGLE_CHOICE), null, Map.of()).isEmpty()).isTrue();
    }

    @Test
    void freeTextLongerThanLimitIsRejectedInsteadOfTruncated() {
        FormOption other = option("other", true);

        assertThatThrownBy(() -> AnswerValidator.parse(question(QuestionType.SINGLE_CHOICE),
                choices(new ChoiceInputDTO("other", "x".repeat(AnswerValidator.FREE_TEXT_MAX_LENGTH + 1))), options(other)))
                .hasMessageContaining("excede");
    }

    // ── texto ─────────────────────────────────────────────────────────────────

    @Test
    void blankTextIsEmpty() {
        assertThat(AnswerValidator.parse(question(QuestionType.TEXT), text("   "), Map.of()).isEmpty()).isTrue();
        assertThat(AnswerValidator.parse(question(QuestionType.TEXT), null, Map.of()).isEmpty()).isTrue();
    }

    @Test
    void textIsTrimmedAndRejectedAboveHardLimit() {
        assertThat(AnswerValidator.parse(question(QuestionType.TEXT), text("  oi  "), Map.of()).text()).isEqualTo("oi");

        assertThatThrownBy(() -> AnswerValidator.parse(question(QuestionType.TEXT),
                text("x".repeat(AnswerValidator.TEXT_MAX_LENGTH + 1)), Map.of()))
                .hasMessageContaining("máximo");
        // o mesmo tamanho é aceito em texto longo
        assertThat(AnswerValidator.parse(question(QuestionType.LONG_TEXT),
                text("x".repeat(AnswerValidator.TEXT_MAX_LENGTH + 1)), Map.of()).isEmpty()).isFalse();
    }

    @Test
    void textLengthBoundsFromQuestion() {
        FormQuestion q = question(QuestionType.TEXT);
        q.setMinValue(6);
        q.setMaxValue(6);

        assertThat(AnswerValidator.parse(q, text("123456"), Map.of()).text()).isEqualTo("123456");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("12345"), Map.of())).hasMessageContaining("mínimo");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("1234567"), Map.of())).hasMessageContaining("máximo");
    }

    @Test
    void integerFormatParsesNormalizesAndChecksRange() {
        FormQuestion q = question(QuestionType.TEXT);
        q.setTextFormat(TextFormat.INTEGER);
        q.setMinValue(1960);
        q.setMaxValue(2100);

        assertThat(AnswerValidator.parse(q, text(" 02020 "), Map.of()).text()).isEqualTo("2020");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("abc"), Map.of())).hasMessageContaining("número inteiro");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("1959"), Map.of())).hasMessageContaining("mínimo");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("2101"), Map.of())).hasMessageContaining("máximo");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("99999999999"), Map.of())).hasMessageContaining("número inteiro");
    }

    @Test
    void emailFormat() {
        FormQuestion q = question(QuestionType.TEXT);
        q.setTextFormat(TextFormat.EMAIL);

        assertThat(AnswerValidator.parse(q, text("a@b.co"), Map.of()).text()).isEqualTo("a@b.co");
        assertThatThrownBy(() -> AnswerValidator.parse(q, text("a@b"), Map.of())).hasMessageContaining("e-mail");
    }

    @Test
    void errorMessagesCarryTheQuestionPrompt() {
        assertThatThrownBy(() -> AnswerValidator.parse(question(QuestionType.SINGLE_CHOICE),
                choices(new ChoiceInputDTO("x", null)), Map.of()))
                .hasMessageStartingWith("Pergunta: ");
    }
}
