package com.nova.narrativa.domain.user.dto;

import com.nova.narrativa.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "상태는 필수입니다.")
    private User.Status status;
}