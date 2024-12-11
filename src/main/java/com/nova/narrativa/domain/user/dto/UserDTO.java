package com.nova.narrativa.domain.user.dto;

import com.nova.narrativa.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String profileUrl;
    private User.Role role;
    private User.Status status;
    private User.LoginType loginType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}