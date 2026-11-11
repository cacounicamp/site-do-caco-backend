package com.caco.sitedocaco.infrastructure.storage.imgBB;

import com.caco.sitedocaco.shared.storage.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImgBBClient {

    private final ImgBBConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String upload(byte[] content, String originalFilename) {
        String base64 = Base64.getEncoder().encodeToString(content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", config.getApiKey());
        body.add("image", base64);
        if (originalFilename != null && !originalFilename.isBlank()) {
            body.add("name", originalFilename);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(config.getApiUrl(), request, Map.class);
        } catch (Exception e) {
            log.error("Erro de rede ao enviar arquivo para ImgBB", e);
            throw new StorageException("Falha de rede ao enviar arquivo para o servidor de armazenamento.", e);
        }

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new StorageException("Resposta inválida do servidor de armazenamento: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();

        if (!Boolean.TRUE.equals(responseBody.get("success"))) {
            throw new StorageException("Servidor de armazenamento reportou falha no upload.");
        }

        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        if (data == null || data.get("url") == null) {
            throw new StorageException("Resposta do servidor de armazenamento sem URL.");
        }

        return (String) data.get("url");
    }
}