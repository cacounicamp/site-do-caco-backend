package com.caco.sitedocaco.modules.media.infrastructure;

import com.caco.sitedocaco.shared.config.ImgBBConfig;
import com.caco.sitedocaco.shared.entity.ImageType;
import com.caco.sitedocaco.shared.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImgBBService {

    private final ImgBBConfig imgBBConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Faz upload de uma imagem para o ImgBB
     * @param file Arquivo de imagem
     * @return URL pública da imagem no ImgBB
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Converter para Base64
        byte[] fileBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);

        // Fazer upload para ImgBB
        return uploadToImgBB(base64Image, file.getOriginalFilename());
    }

    /**
     * Faz upload de uma imagem para o ImgBB
     * @param file Arquivo de imagem
     * @param imageType Tipo da imagem (para validação)
     * @return URL pública da imagem no ImgBB
     */
    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        validateImage(file, imageType);

        return uploadImage(file);
    }

    /**
     * Upload direto com Base64 (útil para processar antes)
     */
    public String uploadImage(String base64Image, String fileName, ImageType imageType) throws IOException {
        // Decodificar para validar
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        validateImageBytes(imageBytes, imageType);

        return uploadToImgBB(base64Image, fileName);
    }

    private String uploadToImgBB(String base64Image, String fileName) {
        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Criar body da requisição
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", imgBBConfig.getApiKey());
        body.add("image", base64Image);
        if (fileName != null) {
            body.add("name", fileName);
        }

        // Criar request
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Fazer a requisição
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    imgBBConfig.getApiUrl(),
                    request,
                    Map.class
            );

            // Verificar resposta
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Falha ao fazer upload para ImgBB");
            }

            Map<String, Object> responseBody = response.getBody();

            // Verificar sucesso
            Boolean success = (Boolean) responseBody.get("success");
            if (success == null || !success) {
                throw new RuntimeException("Falha ao fazer upload para ImgBB");
            }

            // Extrair URL da imagem
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            return (String) data.get("url");

        } catch (Exception e) {
            log.error("Erro ao fazer upload para ImgBB: {}", e.getMessage());
            throw new RuntimeException("Falha ao fazer upload da imagem: " + e.getMessage(), e);
        }
    }

    private void validateImage(MultipartFile file, ImageType imageType) throws IOException {
        // Verificar tamanho
        if (file.getSize() > imageType.getMaxSizeBytes()) {
            throw new BusinessRuleException(
                    String.format("Imagem muito grande. Máximo permitido: %dMB",
                            imageType.getMaxSizeBytes() / (1024 * 1024))
            );
        }

        // Verificar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessRuleException("Arquivo não é uma imagem válida");
        }

        // Ler e validar dimensões
        byte[] bytes = file.getBytes();
        validateImageBytes(bytes, imageType);
    }

    private void validateImageBytes(byte[] imageBytes, ImageType imageType) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new BusinessRuleException("Não foi possível ler a imagem");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        double aspectRatio = (double) width / height;
        double targetAspectRatio = imageType.getAspectRatio();

        // Verificar aspect ratio (com 5% de tolerância)
        if (Math.abs(aspectRatio - targetAspectRatio) > targetAspectRatio * 0.05) {
            throw new BusinessRuleException(
                    String.format("Aspect ratio incorreto. Esperado: %d:%d. Recebido: %d:%d",
                            imageType.getAspectRatioWidth(), imageType.getAspectRatioHeight(),
                            width, height)
            );
        }

        // Verificar dimensões máximas
        if (width > imageType.getMaxWidth() || height > imageType.getMaxHeight()) {
            throw new BusinessRuleException(
                    String.format("Dimensões muito grandes. Máximo: %dx%d",
                            imageType.getMaxWidth(), imageType.getMaxHeight())
            );
        }
    }


    // =================== PDF UPLOAD  ===================
    /**
     * Upload de arquivo com nome customizado
     *
     * @param file Arquivo a ser enviado
     * @param customName Nome customizado (opcional)
     * @return URL pública do arquivo
     * @throws IOException Se ocorrer erro ao ler o arquivo
     */
    public String uploadFile(MultipartFile file, String customName) throws IOException {
        // Validar tamanho máximo (32MB - limite do ImgBB)
        long maxSize = 32 * 1024 * 1024; // 32MB em bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("Arquivo muito grande. Máximo: %dMB", maxSize / (1024 * 1024))
            );
        }

        // Converter para Base64
        byte[] fileBytes = file.getBytes();
        String base64File = Base64.getEncoder().encodeToString(fileBytes);

        // Usar nome do arquivo original se customName for null
        String fileName = customName != null ? customName : file.getOriginalFilename();

        // Fazer upload
        return uploadToImgBB(base64File, fileName);
    }

    /**
     * Upload específico para PDF com validação
     *
     * @param pdfFile Arquivo PDF
     * @param customName Nome customizado (opcional)
     * @return URL pública do PDF
     * @throws IOException Se não for PDF válido
     */
    public String uploadPdf(MultipartFile pdfFile, String customName) throws IOException {
        // Validar se é PDF
        String contentType = pdfFile.getContentType();
        String originalName = pdfFile.getOriginalFilename();

        boolean isPdf = "application/pdf".equals(contentType) ||
                (originalName != null && originalName.toLowerCase().endsWith(".pdf"));

        if (!isPdf) {
            throw new IllegalArgumentException("O arquivo deve ser um PDF válido");
        }

        return uploadFile(pdfFile, customName);
    }

    /**
     * Upload específico para PDF sem nome customizado
     */
    public String uploadPdf(MultipartFile pdfFile) throws IOException {
        return uploadPdf(pdfFile, null);
    }
}