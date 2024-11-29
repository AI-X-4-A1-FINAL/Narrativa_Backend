package com.nova.narrativa.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SocialLoginResult {

    private String id;
    private String nickname;
    private String profile_image_url;
}
