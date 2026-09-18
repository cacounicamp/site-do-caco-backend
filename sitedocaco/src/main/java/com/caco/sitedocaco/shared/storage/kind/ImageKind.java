package com.caco.sitedocaco.shared.storage.kind;

import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import com.caco.sitedocaco.shared.storage.UploadRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public enum ImageKind implements FileKind {

    PROFILE_AVATAR("profile-avatar", AspectRatio.SQUARE, 1024, 1024, 2L  * 1024 * 1024),
    EVENT_COVER   ("event-cover",    AspectRatio.R_4_3,  1200, 900,  8L  * 1024 * 1024),
    NEWS_COVER    ("news-cover",     AspectRatio.R_3_2,  2400, 1600, 5L  * 1024 * 1024),
    BANNER        ("banner",         AspectRatio.R_21_9, 1920, 822,  8L  * 1024 * 1024),
    STICKER       ("sticker",        AspectRatio.SQUARE, 4000, 4000, 10L * 1024 * 1024),
    GALLERY_ITEM  ("gallery-item",   null,               4000, 4000, 10L * 1024 * 1024);

    private static final double ASPECT_TOLERANCE = 0.05; // 5%

    private final String code;
    private final AspectRatio aspectRatio; // null = proporção livre
    private final int maxWidth;
    private final int maxHeight;
    private final long maxSizeBytes;

    ImageKind(String code, AspectRatio aspectRatio, int maxWidth, int maxHeight, long maxSizeBytes) {
        this.code = code;
        this.aspectRatio = aspectRatio;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxSizeBytes = maxSizeBytes;
    }

    @Override
    public String code() { return code; }

    @Override
    public Category category() { return Category.IMAGE; }

    @Override
    public void validate(UploadRequest request) {
        if (request.sizeBytes() > maxSizeBytes) {
            throw new BusinessRuleException(String.format(
                    "Imagem '%s' excede o tamanho máximo de %dMB.",
                    code, maxSizeBytes / (1024 * 1024)));
        }

        String contentType = request.contentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessRuleException(
                    "Arquivo '" + code + "' deve ser uma imagem.");
        }

        BufferedImage image = readImage(request);
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > maxWidth || height > maxHeight) {
            throw new BusinessRuleException(String.format(
                    "Imagem '%s' excede dimensões máximas de %dx%d (recebido %dx%d).",
                    code, maxWidth, maxHeight, width, height));
        }

        if (aspectRatio != null) {
            double actual = (double) width / height;
            double target = aspectRatio.ratio();
            if (Math.abs(actual - target) > target * ASPECT_TOLERANCE) {
                throw new BusinessRuleException(String.format(
                        "Imagem '%s' deve ter proporção %s (recebido %dx%d).",
                        code, aspectRatio, width, height));
            }
        }
    }

    private static BufferedImage readImage(UploadRequest request) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(request.content()));
            if (image == null) {
                throw new BusinessRuleException(
                        "Não foi possível interpretar a imagem como formato válido.");
            }
            return image;
        } catch (IOException e) {
            throw new BusinessRuleException("Falha ao ler os bytes da imagem: " + e.getMessage());
        }
    }
}