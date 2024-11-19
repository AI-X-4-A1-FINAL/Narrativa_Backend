package com.nova.narrativa.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SignUp {

    private Long user_id;
    private String username;
    private String profile_url;
    private String login_type;
}
