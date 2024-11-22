package com.nova.narrativa.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfileInfo {

    private String nickname;
    private String profile_url;
}
