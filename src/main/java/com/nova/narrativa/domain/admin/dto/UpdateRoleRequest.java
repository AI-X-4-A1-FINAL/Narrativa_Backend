package com.nova.narrativa.domain.admin.dto;

import com.nova.narrativa.domain.admin.entity.AdminUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateRoleRequest {
    private AdminUser.Role role;
}