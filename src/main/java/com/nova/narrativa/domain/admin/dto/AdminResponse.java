package com.nova.narrativa.domain.admin.dto;

import com.nova.narrativa.domain.admin.entity.AdminUser;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminResponse {
    private Long id;
    private String username;
    private String email;
    private AdminUser.Role role;
    private AdminUser.Status status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminResponse from(AdminUser admin) {
        return AdminResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .role(admin.getRole())
                .status(admin.getStatus())
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }
}
