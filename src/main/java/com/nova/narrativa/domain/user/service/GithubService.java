package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.api.GithubApi;
import com.nova.narrativa.domain.user.dto.GithubLoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubApi githubApi;

    public GithubLoginResult githubLogin(String AuthCode) throws Exception {

        // 2. 토큰 받기
        String accessToken = githubApi.getUserInfo(AuthCode);
        log.info("accessToken = {}", accessToken);

        // 3. 사용자 정보 받기
        GithubLoginResult userInfoWithToken = githubApi.getUserInfoWithToken(accessToken);
        log.info("userInfoWithToken = {}", userInfoWithToken);

        return userInfoWithToken;
    }
}
