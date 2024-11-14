package com.nova.narrativa.domain.login.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GoogleLoginResult {

    private Long id;
    private String nickname;
    private String profile_image_url;
    private String email;
}
