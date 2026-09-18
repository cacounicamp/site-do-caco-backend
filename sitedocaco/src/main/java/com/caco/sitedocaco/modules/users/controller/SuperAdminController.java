package com.caco.sitedocaco.modules.users.controller;

import com.caco.sitedocaco.modules.users.dto.request.ChangeUserRoleDTO;
import com.caco.sitedocaco.modules.users.dto.response.UserResponseDTO;
import com.caco.sitedocaco.shared.security.ratelimit.RateLimit;
import com.caco.sitedocaco.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/super-admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RateLimit(capacity = 30, refillTokens = 30)
public class SuperAdminController {

    private final UserService userService;

    /**
     * Lista todos os usuários (paginado).
     * GET /api/super-admin/users?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> listAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.listAllUsers(pageable));
    }

    /**
     * Retorna os detalhes completos de um usuário pelo ID.
     * GET /api/super-admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserDetails(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    /**
     * Promove ou demove o role de um usuário.
     * PUT /api/super-admin/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<UserResponseDTO> changeRole(
            @PathVariable UUID userId,
            @RequestBody @Valid ChangeUserRoleDTO dto
    ) {
        return ResponseEntity.ok(userService.changeUserRole(userId, dto.role()));
    }

    /**
     * Suspende a conta de um usuário.
     * PUT /api/super-admin/users/{userId}/suspend
     */
    @PutMapping("/{userId}/suspend")
    public ResponseEntity<UserResponseDTO> suspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.suspendUser(userId));
    }

    /**
     * Libera/reativa a conta de um usuário suspenso.
     * PUT /api/super-admin/users/{userId}/unsuspend
     */
    @PutMapping("/{userId}/unsuspend")
    public ResponseEntity<UserResponseDTO> unsuspendUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.unsuspendUser(userId));
    }
}
