package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.util.CustomJWTException;
import com.nova.narrativa.domain.user.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class APIRefreshController {

    @PostMapping("/api/users/refresh")
    public Map<String, Object> refresh(@RequestHeader("Authorization") String authHeader, String refreshToken ) throws Exception {

        if (refreshToken == null)   throw new CustomJWTException("NULL_REFRESH");
        if (authHeader == null || authHeader.length() < 7)   throw new CustomJWTException("INVALID STRING");

        String accessToken = authHeader.substring(7);

        // accessToken 만료 여부 확인
        if (checkExpiredToken(accessToken) == false) {
            return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
        }

        // refreshToken 검증
        Map<String, Object> claims = JWTUtil.validateToken(refreshToken);

        log.info("refresh claims: ", claims);

        String newAccessToken = JWTUtil.generateToken(claims, 10);
        String newRefreshToken = checkTime((Integer) claims.get("exp")) == true ? JWTUtil.generateToken(claims, 60 * 24) : refreshToken;

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    // 시간이 1시간 미만으로 남았다면
    private boolean checkTime(Integer exp) {

        // JWT exp를 날짜로 변환
        Date expDate = new Date((long) exp * 1000);

        // 현재 시간과의 차이 계산 - 밀리세컨즈
        long gap = expDate.getTime() - new Date().getTime();

        // 분단위 계산
        long leftMin = gap / (1000 * 60);

        // 1시간도 안남은 경우
        return leftMin < 60;
    }

    private boolean checkExpiredToken(String token) {
        try {
            JWTUtil.validateToken(token);
        } catch (CustomJWTException ex) {
            if (ex.getMessage().equals("Expired")) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
