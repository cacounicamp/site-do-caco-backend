package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.shared.storage.kind.DocumentKind;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Valida um upload contra a configuração (ou o padrão) da pergunta: extensão e tamanho. É uma
 * política MAIS restritiva que a de {@link DocumentKind#FORM_ATTACHMENT}, que continua rodando por
 * cima disso como teto/checagem de assinatura de bytes (ver {@code FormSubmissionService}).
 */
final class FileAnswerValidator {

    static final int DEFAULT_MAX_SIZE_BYTES = 3 * 1024 * 1024;

    private FileAnswerValidator() {
    }

    static void validate(FormQuestion question, MultipartFile file) {
        Set<String> allowed = effectiveExtensions(question);
        String extension = extensionOf(file.getOriginalFilename());
        if (!allowed.contains(extension)) {
            throw AnswerValidator.error(question, "extensão não permitida; use: "
                    + new TreeSet<>(allowed).stream().map(e -> e.toUpperCase(Locale.ROOT)).collect(Collectors.joining(", ")) + ".");
        }

        int max = effectiveMaxSizeBytes(question);
        if (file.getSize() > max) {
            throw AnswerValidator.error(question, "arquivo excede o tamanho máximo de " + (max / (1024 * 1024)) + "MB.");
        }
    }

    /** Vazio/nulo configurado na pergunta = aceita qualquer extensão do catálogo suportado. */
    static Set<String> effectiveExtensions(FormQuestion question) {
        Set<String> configured = question.getAllowedExtensions();
        return configured == null || configured.isEmpty() ? DocumentKind.supportedFormAttachmentExtensions() : configured;
    }

    static int effectiveMaxSizeBytes(FormQuestion question) {
        return question.getMaxFileSizeBytes() != null ? question.getMaxFileSizeBytes() : DEFAULT_MAX_SIZE_BYTES;
    }

    private static String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
