package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.api.KakaoApi;
import com.nova.narrativa.domain.user.dto.KakaoLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final KakaoApi kakaoApi;

    public KakaoLoginResult kakaoLogin(String AuthCode) throws Exception {
//        // 1. 인가 코드 받기
//        String AuthCode = kakaoApi.getAuthCode();
//        System.out.println("AuthCode = " + AuthCode);

        // 2. 토큰 받기
        String accessToken = kakaoApi.getUserInfo(AuthCode);
        log.info("accessToken = {}", accessToken);

        // 3. 사용자 정보 받기
        KakaoLoginResult userInfoWithToken = kakaoApi.getUserInfoWithToken(accessToken);
        log.info("userInfoWithToken = {}", userInfoWithToken);

        return userInfoWithToken;
    }
}
