package com.nova.narrativa.domain.login.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KakaoLoginResult {

    private long id;
    private String nickname;
    private String profile_image_url;
}
