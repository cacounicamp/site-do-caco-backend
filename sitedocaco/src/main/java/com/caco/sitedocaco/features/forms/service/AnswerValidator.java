package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.dto.request.AnswerInputDTO;
import com.caco.sitedocaco.features.forms.dto.request.ChoiceInputDTO;
import com.caco.sitedocaco.features.forms.entity.FormOption;
import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.features.forms.entity.QuestionType;
import com.caco.sitedocaco.features.forms.entity.TextFormat;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Valida e normaliza a resposta de UMA pergunta de texto ou de escolha, sem acesso a banco. */
final class AnswerValidator {

    static final int TEXT_MAX_LENGTH = 255;
    static final int LONG_TEXT_MAX_LENGTH = 2000;
    static final int FREE_TEXT_MAX_LENGTH = 200;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    record Choice(FormOption option, String freeText) {
    }

    record Parsed(String text, List<Choice> choices) {
        boolean isEmpty() {
            return text.isEmpty() && choices.isEmpty();
        }
    }

    private AnswerValidator() {
    }

    /**
     * @param optionsByCode opções ATIVAS do conjunto da pergunta, por código (vazio para perguntas de texto)
     * @param input         resposta enviada; nulo se a pergunta não veio no payload
     */
    static Parsed parse(FormQuestion question, AnswerInputDTO input, Map<String, FormOption> optionsByCode) {
        QuestionType type = question.getType();
        if (type.isChoice()) {
            return parseChoices(question, input, optionsByCode);
        }
        if (type.isText()) {
            return parseText(question, input);
        }
        throw new IllegalStateException("Perguntas do tipo " + type + " não passam por este validador.");
    }

    private static Parsed parseChoices(FormQuestion question, AnswerInputDTO input, Map<String, FormOption> optionsByCode) {
        List<ChoiceInputDTO> items = input == null || input.options() == null ? List.of() : input.options();
        if (question.getType() == QuestionType.SINGLE_CHOICE && items.size() > 1) {
            throw error(question, "escolha apenas uma opção.");
        }

        List<Choice> choices = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ChoiceInputDTO item : items) {
            FormOption option = optionsByCode.get(item.option());
            if (option == null) {
                throw error(question, "opção inválida '" + item.option() + "'.");
            }
            if (!seen.add(option.getCode())) {
                throw error(question, "opção repetida '" + option.getCode() + "'.");
            }

            String freeText = item.freeText() == null ? "" : item.freeText().strip();
            if (option.isAllowsFreeText() && freeText.isEmpty()) {
                throw error(question, "especifique o valor para \"" + option.getLabel() + "\".");
            }
            if (freeText.length() > FREE_TEXT_MAX_LENGTH) {
                throw error(question, "o texto de \"" + option.getLabel() + "\" excede " + FREE_TEXT_MAX_LENGTH + " caracteres.");
            }
            choices.add(new Choice(option, option.isAllowsFreeText() ? freeText : null));
        }
        return new Parsed("", choices);
    }

    private static Parsed parseText(FormQuestion question, AnswerInputDTO input) {
        String text = input == null || input.text() == null ? "" : input.text().strip();
        if (text.isEmpty()) {
            return new Parsed("", List.of());
        }

        int hardLimit = question.getType() == QuestionType.LONG_TEXT ? LONG_TEXT_MAX_LENGTH : TEXT_MAX_LENGTH;
        if (text.length() > hardLimit) {
            throw error(question, "máximo de " + hardLimit + " caracteres.");
        }

        if (question.getTextFormat() == TextFormat.INTEGER) {
            return new Parsed(parseInteger(question, text), List.of());
        }
        if (question.getTextFormat() == TextFormat.EMAIL && !EMAIL.matcher(text).matches()) {
            throw error(question, "e-mail inválido.");
        }

        if (question.getMinValue() != null && text.length() < question.getMinValue()) {
            throw error(question, "mínimo de " + question.getMinValue() + " caracteres.");
        }
        if (question.getMaxValue() != null && text.length() > question.getMaxValue()) {
            throw error(question, "máximo de " + question.getMaxValue() + " caracteres.");
        }
        return new Parsed(text, List.of());
    }

    private static String parseInteger(FormQuestion question, String text) {
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw error(question, "informe um número inteiro.");
        }
        if (question.getMinValue() != null && value < question.getMinValue()) {
            throw error(question, "valor mínimo é " + question.getMinValue() + ".");
        }
        if (question.getMaxValue() != null && value > question.getMaxValue()) {
            throw error(question, "valor máximo é " + question.getMaxValue() + ".");
        }
        return String.valueOf(value);
    }

    static BusinessRuleException error(FormQuestion question, String detail) {
        return new BusinessRuleException(question.getPrompt() + ": " + detail);
    }
}
