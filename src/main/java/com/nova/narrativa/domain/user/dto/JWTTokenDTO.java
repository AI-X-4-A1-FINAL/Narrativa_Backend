package com.nova.narrativa.domain.user.dto;

import com.nova.narrativa.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class JWTTokenDTO {

    private String id;
    private String userId;
    private String username;
    private String profile_url;
    private User.Role role;
    private User.Status status;
    private User.LoginType loginType;

    public Map<String, Object> getClaims() {

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", id);
        dataMap.put("user_id", userId);
        dataMap.put("username", username);
        dataMap.put("profile_url", profile_url);
        dataMap.put("role", role);
        dataMap.put("login_type", loginType);

        return dataMap;
    }
}