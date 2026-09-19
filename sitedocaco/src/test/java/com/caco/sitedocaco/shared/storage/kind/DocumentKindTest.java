package com.caco.sitedocaco.shared.storage.kind;

import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.storage.UploadRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentKindTest {

    private static UploadRequest request(DocumentKind kind, String filename, String contentType, byte[] content) {
        return new UploadRequest(kind, content, filename, contentType);
    }

    private static byte[] pdf() {
        return "%PDF-1.7 corpo".getBytes(StandardCharsets.ISO_8859_1);
    }

    @Test
    void formAttachmentAcceptsAllowedExtensionsWithMatchingSignature() {
        DocumentKind kind = DocumentKind.FORM_ATTACHMENT;

        assertThatCode(() -> kind.validate(request(kind, "cv.PDF", "application/pdf", pdf()))).doesNotThrowAnyException();
        assertThatCode(() -> kind.validate(request(kind, "cv.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0}))).doesNotThrowAnyException();
        assertThatCode(() -> kind.validate(request(kind, "cv.jpeg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0}))).doesNotThrowAnyException();
        assertThatCode(() -> kind.validate(request(kind, "cv.docx", "application/octet-stream",
                new byte[]{'P', 'K', 3, 4, 0}))).doesNotThrowAnyException();
    }

    @Test
    void formAttachmentRejectsDisallowedExtension() {
        DocumentKind kind = DocumentKind.FORM_ATTACHMENT;

        assertThatThrownBy(() -> kind.validate(request(kind, "virus.exe", "application/pdf", pdf())))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PDF, DOC, DOCX, JPG ou PNG");
        assertThatThrownBy(() -> kind.validate(request(kind, "semextensao", "application/pdf", pdf())))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> kind.validate(request(kind, null, "application/pdf", pdf())))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void formAttachmentRejectsContentThatDoesNotMatchTheExtension() {
        DocumentKind kind = DocumentKind.FORM_ATTACHMENT;

        assertThatThrownBy(() -> kind.validate(request(kind, "fake.pdf", "application/pdf", "MZ executavel".getBytes())))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não corresponde");
        assertThatThrownBy(() -> kind.validate(request(kind, "vazio.pdf", "application/pdf", new byte[0])))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void formAttachmentRejectsFilesAbove3MB() {
        DocumentKind kind = DocumentKind.FORM_ATTACHMENT;
        byte[] big = new byte[3 * 1024 * 1024 + 1];
        System.arraycopy(pdf(), 0, big, 0, pdf().length);

        assertThatThrownBy(() -> kind.validate(request(kind, "cv.pdf", "application/pdf", big)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3MB");
    }

    @Test
    void examPdfKeepsItsOriginalLenientRule() {
        DocumentKind kind = DocumentKind.EXAM_PDF;

        // aceita por MIME ou por extensão, sem checar assinatura (comportamento anterior à FORM_ATTACHMENT)
        assertThatCode(() -> kind.validate(request(kind, "prova", "application/pdf", "qualquer".getBytes()))).doesNotThrowAnyException();
        assertThatCode(() -> kind.validate(request(kind, "prova.pdf", "text/plain", "qualquer".getBytes()))).doesNotThrowAnyException();
        assertThatThrownBy(() -> kind.validate(request(kind, "prova.png", "image/png", "qualquer".getBytes())))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("deve ser um PDF");
    }
}
