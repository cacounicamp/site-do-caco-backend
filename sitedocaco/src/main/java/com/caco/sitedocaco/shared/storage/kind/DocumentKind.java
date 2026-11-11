package com.caco.sitedocaco.shared.storage.kind;

import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.storage.UploadRequest;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum DocumentKind implements FileKind {

    EXAM_PDF("exam-pdf", 32L * 1024 * 1024) {
        @Override
        void validateContent(UploadRequest request) {
            boolean isPdfMime = "application/pdf".equals(request.contentType());
            boolean isPdfExt  = request.originalFilename() != null
                    && request.originalFilename().toLowerCase().endsWith(".pdf");

            if (!isPdfMime && !isPdfExt) {
                throw new BusinessRuleException(
                        "Documento '" + code() + "' deve ser um PDF.");
            }
        }
    },

    /**
     * Anexo enviado por usuário em resposta de formulário (ex.: currículo). O admin configura, por
     * pergunta, um subconjunto de {@link #supportedFormAttachmentExtensions()} e um tamanho máximo
     * mais restritivo (ver {@code features.forms.service.FileAnswerValidator}); o limite abaixo é
     * só o teto absoluto de segurança, alinhado a {@code spring.servlet.multipart.max-file-size}.
     */
    FORM_ATTACHMENT("form-attachment", 15L * 1024 * 1024) {
        @Override
        void validateContent(UploadRequest request) {
            List<byte[]> signatures = SIGNATURES.get(extensionOf(request.originalFilename()));
            if (signatures == null) {
                throw new BusinessRuleException(
                        "Arquivo '" + code() + "' deve ser PDF, DOC, DOCX, JPG ou PNG.");
            }
            // Não confia só no nome que o cliente mandou: confere os primeiros bytes do arquivo.
            if (signatures.stream().noneMatch(signature -> startsWith(request.content(), signature))) {
                throw new BusinessRuleException(
                        "O conteúdo do arquivo '" + code() + "' não corresponde à extensão informada.");
            }
        }
    };

    private static final Map<String, List<byte[]>> SIGNATURES = Map.of(
            "pdf",  List.of(new byte[]{'%', 'P', 'D', 'F', '-'}),
            "jpg",  List.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "jpeg", List.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "png",  List.of(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}),
            "docx", List.of(new byte[]{'P', 'K', 0x03, 0x04}),
            "doc",  List.of(new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0})
    );

    private final String code;
    private final long maxSizeBytes;

    DocumentKind(String code, long maxSizeBytes) {
        this.code = code;
        this.maxSizeBytes = maxSizeBytes;
    }

    @Override
    public String code() { return code; }

    @Override
    public Category category() { return Category.DOCUMENT; }

    /** Teto absoluto de tamanho desta constante; políticas mais restritivas ficam por conta de quem chama. */
    public long maxSizeBytes() { return maxSizeBytes; }

    /** Extensões que {@link #FORM_ATTACHMENT} sabe verificar por assinatura de bytes. */
    public static Set<String> supportedFormAttachmentExtensions() {
        return SIGNATURES.keySet();
    }

    @Override
    public void validate(UploadRequest request) {
        if (request.sizeBytes() > maxSizeBytes) {
            throw new BusinessRuleException(String.format(
                    "Documento '%s' excede o tamanho máximo de %dMB.",
                    code, maxSizeBytes / (1024 * 1024)));
        }

        validateContent(request);
    }

    abstract void validateContent(UploadRequest request);

    private static String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (content[i] != prefix[i]) return false;
        }
        return true;
    }
}
