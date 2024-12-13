package com.nova.narrativa.domain.admin.controller;

import com.nova.narrativa.domain.admin.dto.*;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AdminService;
import com.nova.narrativa.domain.admin.util.AdminAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${environments.narrativa-admin.url}",
        allowCredentials = "true",
        allowedHeaders = "*",
        exposedHeaders = "*")
public class AdminController {
    private final AdminService adminService;

    @AdminAuth
    @GetMapping("/users")
    public ResponseEntity<?> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(Map.of("data", admins));
    }

    @AdminAuth
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<?> updateAdminStatus(
            @PathVariable Long userId,
            @RequestBody UpdateStatusRequest request) {
        try {
            AdminResponse updatedAdmin = adminService.updateStatus(userId, request.getStatus());
            return ResponseEntity.ok(Map.of(
                    "message", "관리자 상태가 업데이트되었습니다.",
                    "data", updatedAdmin
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @AdminAuth
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> updateAdminRole(
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request) {
        try {
            AdminUser updatedUser = adminService.updateAdminRole(userId, request.getRole());
            return ResponseEntity.ok(Map.of(
                    "message", "관리자 권한이 업데이트되었습니다: " + updatedUser.getRole(),
                    "data", AdminResponse.from(updatedUser)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}