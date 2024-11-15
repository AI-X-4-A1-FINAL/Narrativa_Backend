package com.nova.narrativa.domain.user.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.nova.narrativa.common.util.JsonParse;
import com.nova.narrativa.domain.user.dto.GoogleLoginResult;
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
public class GoogleApi {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URL;

    private final static String GOOGLE_AUTH_URI = "https://oauth2.googleapis.com";
    private final static String GOOGLE_API_URI = "https://www.googleapis.com";

    public String getUserInfo(String code) throws Exception {
        if (code == null) throw new Exception("Failed get authorization code");

        String accessToken;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", GOOGLE_CLIENT_ID);
            params.add("client_secret", GOOGLE_CLIENT_SECRET);
            params.add("code", code);
            params.add("redirect_uri", GOOGLE_REDIRECT_URL);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_AUTH_URI + "/token",
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

    public GoogleLoginResult getUserInfoWithToken(String accessToken) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(
                GOOGLE_API_URI + "/oauth2/v3/userinfo",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JsonNode jsonObject = JsonParse.parse(response.getBody());
        log.info("jsonObject = {}", jsonObject);

        long id = jsonObject.get("sub").asLong();
        String nickname = jsonObject.get("name").asText();
        String picture_url = jsonObject.get("picture").asText();
        String email = jsonObject.get("email").asText();

        return GoogleLoginResult.builder()
                .id(id)
                .nickname(nickname)
                .profile_image_url(picture_url)
                .email(email)
                .build();
    }
}
