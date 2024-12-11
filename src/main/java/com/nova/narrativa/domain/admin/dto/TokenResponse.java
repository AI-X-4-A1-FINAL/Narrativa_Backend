package com.nova.narrativa.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String uid;
    private String email;
    private String role;
    private String username;
}