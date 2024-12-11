package com.nova.narrativa.domain.user.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RequestParseUtil {

    //
    public static Long getSeq(HttpServletRequest request) {
        // 필터에서 설정한 userClaims 정보를 가져옴
        Map<String, Object> claims = (Map<String, Object>) request.getAttribute("claims");

        log.info("claims: {}", claims);

        // id 값을 안전하게 Long 타입으로 변환
        Long seq = null;

        if (claims != null && claims.containsKey("id")) {
            Object idValue = claims.get("id");
            try {
                if (idValue instanceof Number) {
                    seq = ((Number) idValue).longValue();
                } else {
                    seq = Long.valueOf(idValue.toString());
                }
            } catch (NumberFormatException e) {
                log.error("id 값 변환 실패: {}", idValue, e);
            }
        }

        log.info("id: {}", seq);
        return seq;
    }
}
