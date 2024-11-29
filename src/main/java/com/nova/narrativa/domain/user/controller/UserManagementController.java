package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.UpdateRoleRequest;
import com.nova.narrativa.domain.user.dto.UpdateStatusRequest;
import com.nova.narrativa.domain.user.dto.UserDTO;
import com.nova.narrativa.domain.user.service.UserManagementService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userService;

    // application.yml의 narrativa-admin URL 값 주입
    @Value("${environments.narrativa-admin.url}")
    private String narrativaAdminUrl;

    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username
    ) {
        try {
            Page<UserDTO> users = userService.getUsers(page, size, username);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            // 잘못된 입력 값에 대한 에러 처리
            return ResponseEntity.badRequest().body(null);
        }
    }

    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoleRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            // 검증 오류 처리
            return ResponseEntity.badRequest().body(null);
        }

        try {
            UserDTO updatedUser = userService.updateRole(id, request.getRole());
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStatusRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            // 검증 오류 처리
            return ResponseEntity.badRequest().body(null);
        }

        try {
            UserDTO updatedUser = userService.updateStatus(id, request.getStatus());
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            // 사용자를 찾을 수 없는 경우
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
