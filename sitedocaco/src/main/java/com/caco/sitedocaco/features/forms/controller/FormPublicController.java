package com.caco.sitedocaco.features.forms.controller;

import com.caco.sitedocaco.features.forms.dto.response.FormDTO;
import com.caco.sitedocaco.features.forms.service.FormPublicService;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/forms")
@RequiredArgsConstructor
@RateLimit
public class FormPublicController {

    private final FormPublicService formPublicService;

    /** Definição do formulário (perguntas, opções e condicionais). Rascunhos respondem 404. */
    @GetMapping("/{slug}")
    public ResponseEntity<FormDTO> getForm(@PathVariable String slug) {
        return ResponseEntity.ok(formPublicService.getForm(slug));
    }
}
