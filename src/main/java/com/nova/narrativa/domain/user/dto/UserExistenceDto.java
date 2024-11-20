package com.nova.narrativa.domain.user.dto;

import com.nova.narrativa.domain.user.entity.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserExistenceDto {

    private String userId;
    private User.LoginType loginType;
}
