package com.nova.narrativa.domain.user.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JWTUtil {

    private static final String key = "skoqfbqw0j5f748b45j12qw54ofbojqbeofgjeqbgjqebjofjwbejkfwejfkbwjkefbkjwwefe1212542213efbbo";

    // JWT 문자열 생성
    public static String generateToken(Map<String, Object> valueMap, int min) {
        log.info("valueMap: {}, min: {}, key: {}", valueMap, min, JWTUtil.key);
        if (JWTUtil.key.length() < 32)  throw new RuntimeException("JWT TOKEN 생성시 키 값은 32자 이상이어야 합니다.");
        SecretKey secretKey;

        try {
            secretKey = Keys.hmacShaKeyFor(JWTUtil.key.getBytes(StandardCharsets.UTF_8));
            log.info("secretKey: {}", secretKey);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        String jwtStr = Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(valueMap)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(secretKey)
                .compact();
        log.info("jwtStr: {}", jwtStr);

        return jwtStr;
    }

    // JWT 문자열 검증
    public static Map<String, Object> validateToken(String token) throws Exception{

        Map<String, Object> claim = null;

        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));

            claim = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)  // 피싱 및 검증, 실패 시 에러
                    .getBody();
        } catch (WeakKeyException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("WeakKeyException");
        } catch (ExpiredJwtException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("ExpiredJwtException");
        } catch (UnsupportedJwtException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("UnsupportedJwtException");
        } catch (MalformedJwtException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("MalformedJwtException");
        } catch (SignatureException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("SignatureException");
        } catch (IllegalArgumentException e) {
            log.info("error: {}", e.getMessage());
            throw new RuntimeException("IllegalArgumentException");
        }
        return claim;
    }
}
