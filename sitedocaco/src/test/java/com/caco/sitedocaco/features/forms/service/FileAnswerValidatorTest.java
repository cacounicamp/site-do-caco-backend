package com.caco.sitedocaco.features.forms.service;

import com.caco.sitedocaco.features.forms.entity.FormQuestion;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileAnswerValidatorTest {

    private static FormQuestion question(Set<String> allowedExtensions, Integer maxFileSizeBytes) {
        FormQuestion q = new FormQuestion();
        q.setId(UUID.randomUUID());
        q.setCode("cv");
        q.setPrompt("Currículo");
        q.setAllowedExtensions(allowedExtensions);
        q.setMaxFileSizeBytes(maxFileSizeBytes);
        return q;
    }

    private static MockMultipartFile file(String filename, int sizeBytes) {
        return new MockMultipartFile("file", filename, "application/octet-stream", new byte[sizeBytes]);
    }

    @Test
    void withoutConfigurationUsesTheFullCatalogAndTheDefaultSize() {
        FormQuestion q = question(Set.of(), null);

        assertThatCode(() -> FileAnswerValidator.validate(q, file("cv.pdf", 10))).doesNotThrowAnyException();
        assertThatCode(() -> FileAnswerValidator.validate(q, file("cv.docx", 10))).doesNotThrowAnyException();
        assertThatThrownBy(() -> FileAnswerValidator.validate(q, file("cv.exe", 10)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("extensão não permitida");
        assertThatThrownBy(() -> FileAnswerValidator.validate(q, file("cv.pdf", FileAnswerValidator.DEFAULT_MAX_SIZE_BYTES + 1)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3MB");
    }

    @Test
    void configuredExtensionsRestrictTheCatalogSubset() {
        FormQuestion q = question(Set.of("pdf", "png"), null);

        assertThatCode(() -> FileAnswerValidator.validate(q, file("cv.pdf", 10))).doesNotThrowAnyException();
        assertThatThrownBy(() -> FileAnswerValidator.validate(q, file("cv.docx", 10)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PDF")
                .hasMessageContaining("PNG");
    }

    @Test
    void configuredMaxSizeOverridesTheDefault() {
        FormQuestion q = question(Set.of(), 100);

        assertThatCode(() -> FileAnswerValidator.validate(q, file("cv.pdf", 100))).doesNotThrowAnyException();
        assertThatThrownBy(() -> FileAnswerValidator.validate(q, file("cv.pdf", 101)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("0MB"); // 100 bytes / 1MB arredonda pra 0MB na mensagem, mas o limite em si é respeitado
    }

    @Test
    void extensionIsCaseInsensitiveAndFilenameWithoutExtensionIsRejected() {
        FormQuestion q = question(Set.of(), null);

        assertThatCode(() -> FileAnswerValidator.validate(q, file("CV.PDF", 10))).doesNotThrowAnyException();
        assertThatThrownBy(() -> FileAnswerValidator.validate(q, file("semextensao", 10)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void effectiveExtensionsAndSizeResolveConfiguredOrDefault() {
        assertThat(FileAnswerValidator.effectiveExtensions(question(Set.of(), null))).hasSize(6);
        assertThat(FileAnswerValidator.effectiveExtensions(question(Set.of("pdf"), null))).containsExactly("pdf");
        assertThat(FileAnswerValidator.effectiveMaxSizeBytes(question(Set.of(), null))).isEqualTo(FileAnswerValidator.DEFAULT_MAX_SIZE_BYTES);
        assertThat(FileAnswerValidator.effectiveMaxSizeBytes(question(Set.of(), 500))).isEqualTo(500);
    }
}
