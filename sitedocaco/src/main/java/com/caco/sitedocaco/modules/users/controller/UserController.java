package com.caco.sitedocaco.modules.users.controller;

import com.caco.sitedocaco.modules.users.dto.request.UpdateProfileDTO;
import com.caco.sitedocaco.modules.users.dto.response.UserResponseDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@RateLimit
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getMe());
    }

    // Upload de avatar: limite mais conservador para evitar abuso de bandwidth
    @RateLimit(capacity = 5, refillTokens = 5)
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {

        UpdateProfileDTO dto = new UpdateProfileDTO(name, avatar);
        return ResponseEntity.ok(userService.updateProfile(dto));
    }
}