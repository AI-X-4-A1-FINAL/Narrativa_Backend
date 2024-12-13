package com.nova.narrativa.domain.user.api;

import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class GithubApi {

    @Value("${spring.security.oauth2.registration.github.client-id}")
    private String GITHUB_CLIENT_ID;

    @Value("${spring.security.oauth2.registration.github.client-secret}")
    private String GITHUB_CLIENT_SECRET;

    @Value("${spring.security.oauth2.registration.github.redirect-uri}")
    private String GITHUB_REDIRECT_URL;

    // GitHub API 엔드포인트 상수
    private final static String GITHUB_AUTH_TOKEN_URI = "https://github.com/login/oauth/access_token";
    private final static String GITHUB_API_URI = "https://api.github.com/user";

    public String getUserInfo(String code) throws Exception {
        if (code == null) throw new Exception("Failed get authorization code");

        String accessToken;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // JSON 응답 요청

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", GITHUB_CLIENT_ID);
            params.add("client_secret", GITHUB_CLIENT_SECRET);
            params.add("code", code);
            params.add("redirect_uri", GITHUB_REDIRECT_URL);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);
            log.info("httpEntity");

            ResponseEntity<Map> response = restTemplate.exchange(
                    GITHUB_AUTH_TOKEN_URI,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            log.info("response.getBody(): {}", response.getBody());

            // Map으로 직접 접근
            accessToken = (String) response.getBody().get("access_token");
            log.info("accessToken = {}", accessToken);

            if (accessToken == null || accessToken.isEmpty()) {
                throw new Exception("Failed to get access token");
            }

        } catch (Exception e) {
            log.error("API call Failed: {}", e.getMessage(), e);
            throw new Exception("API call Failed: " + e.getMessage());
        }

        return accessToken;
    }

    public SocialLoginResult getUserInfoWithToken(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // Bearer 토큰 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = rt.exchange(
                GITHUB_API_URI,
                HttpMethod.GET,
                httpEntity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();

        return SocialLoginResult.builder()
                .id(String.valueOf(userInfo.get("id")))
                .nickname((String) userInfo.get("login"))
                .profile_image_url((String) userInfo.get("avatar_url"))
                .build();
    }
}