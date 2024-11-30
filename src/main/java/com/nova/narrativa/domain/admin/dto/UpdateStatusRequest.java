package com.nova.narrativa.domain.admin.dto;

import com.nova.narrativa.domain.admin.entity.AdminUser;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateStatusRequest {
    private AdminUser.Status status;
}