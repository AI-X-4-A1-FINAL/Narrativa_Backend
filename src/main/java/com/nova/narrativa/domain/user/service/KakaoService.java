package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.api.KakaoApi;
import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final KakaoApi kakaoApi;

    public SocialLoginResult kakaoLogin(String AuthCode) throws Exception {

        // 2. 토큰 받기
        String accessToken = kakaoApi.getUserInfo(AuthCode);
        log.info("accessToken = {}", accessToken);

        // 3. 사용자 정보 받기
        SocialLoginResult userInfoWithToken = kakaoApi.getUserInfoWithToken(accessToken);
        log.info("userInfoWithToken = {}", userInfoWithToken);

        return userInfoWithToken;
    }
}