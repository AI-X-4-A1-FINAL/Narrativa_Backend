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

@Slf4j
@Component
public class KakaoApi {

    @Value("${spring.security.oauth2.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${spring.security.oauth2.registration.kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${spring.security.oauth2.registration.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URL;

    private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private final static String KAKAO_API_URI = "https://kapi.kakao.com";

    public String getUserInfo(String code) throws Exception {
        if (code == null) throw new Exception("Failed get authorization code");

        String accessToken;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", KAKAO_CLIENT_ID);
            params.add("client_secret", KAKAO_CLIENT_SECRET);
            params.add("code", code);
            params.add("redirect_uri", KAKAO_REDIRECT_URL);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_AUTH_URI + "/oauth/token",
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            JsonNode node = JsonParse.parse(response.getBody());
            log.info("node = {}", node);
            accessToken = String.valueOf(node.findValue("access_token"));
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
                KAKAO_API_URI + "/v2/user/me",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JsonNode jsonObject = JsonParse.parse(response.getBody());
        log.info("jsonObject = {}", jsonObject);

        // "id"를 가져오기 (long으로 반환)
        String id = jsonObject.get("id").asText();

        // "properties" 객체에서 "nickname"과 "profile_image" 추출
        JsonNode propertiesNode = jsonObject.get("properties");
        String nickname = propertiesNode.get("nickname").asText();
        String profile = propertiesNode.get("profile_image").asText();

        return SocialLoginResult.builder()
                .id(id)
                .nickname(nickname)
                .profile_image_url(profile)
                .build();
    }
}