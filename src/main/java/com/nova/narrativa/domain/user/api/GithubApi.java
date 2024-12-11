package com.nova.narrativa.domain.user.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nova.narrativa.common.util.JsonParse;
import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    private final static String GITHUB_AUTH_URI = "https://github.com";
    private final static String GITHUB_API_URI = "https://api.github.com/user";

    public String getUserInfo(String code) throws Exception {
        if (code == null) throw new Exception("Failed get authorization code");

        String accessToken;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", GITHUB_CLIENT_ID);
            params.add("client_secret", GITHUB_CLIENT_SECRET);
            params.add("code", code);
            params.add("grant_type", "authorization_code");
//            params.add("redirect_uri", "http://localhost:8080/login/github");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    GITHUB_AUTH_URI + "/login/oauth/access_token",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            log.info("response.getBody(): {}", response.getBody());

            // UriComponentsBuilder를 사용하여 쿼리 파라미터 추출
            Map<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + response.getBody()).build().getQueryParams().toSingleValueMap();
            accessToken = queryParams.get("access_token");
            log.info("accessToken = {}", accessToken);
        } catch (Exception e) {
            log.info("API call Failed: {}", e.getMessage());
            throw new Exception("API call Failed");
        }
        return accessToken;
    }

    public SocialLoginResult getUserInfoWithToken(String accessToken) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(
                GITHUB_API_URI,
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        JsonNode jsonObject = JsonParse.parse(response.getBody());
        log.info("jsonObject = {}", jsonObject);

        // "id"를 가져오기 (long으로 반환)
        String id = jsonObject.get("id").asText();
        String nickname = jsonObject.get("login").asText();
        String profile_image_url = jsonObject.get("avatar_url").asText();

        return SocialLoginResult.builder()
                .id(id)
                .nickname(nickname)
                .profile_image_url(profile_image_url)
                .build();
    }
}