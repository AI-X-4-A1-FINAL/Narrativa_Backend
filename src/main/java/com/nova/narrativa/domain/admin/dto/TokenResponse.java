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

    public TokenResponse(String uid, String email, String role) {
        this(uid, email, role, null);
    }
}
