package com.nova.narrativa.domain.login.service;

import com.nova.narrativa.domain.login.api.GoogleApi;
import com.nova.narrativa.domain.login.dto.GoogleLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleService {

    private final GoogleApi googleApi;

    public GoogleLoginResult googleLogin(String AuthCode) throws Exception {

        // 2. 토큰 받기
        String accessToken = googleApi.getUserInfo(AuthCode);
        log.info("accessToken = {}", accessToken);

        // 3. 사용자 정보 받기
        GoogleLoginResult userInfoWithToken = googleApi.getUserInfoWithToken(accessToken);
        log.info("userInfoWithToken = {}", userInfoWithToken);

        return userInfoWithToken;
    }
}
