package com.nova.narrativa.domain.user.dto;

import com.nova.narrativa.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotNull(message = "역할은 필수입니다.")
    private User.Role role;
}
