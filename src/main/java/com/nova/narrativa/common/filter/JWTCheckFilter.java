package com.nova.narrativa.common.filter;

import com.nova.narrativa.domain.user.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class JWTCheckFilter extends OncePerRequestFilter {

    // 특정 경로 필터 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getRequestURI();

        // Swagger 및 특정 경로 필터 제외
        if (path.startsWith("/v3/api-docs") ||           // Swagger 명세 제외
                path.startsWith("/swagger-ui") ||            // Swagger UI 제외
                path.startsWith("/api/users/sign-up") ||     // 회원가입 제외
                path.startsWith("/login") ||                 // 소셜 로그인 제외
                path.startsWith("/api/notices") ||           // 알림 제외
                path.startsWith("/actuator/health") ||       // Health Check 제외
                path.startsWith("/api/admin") ||             // 관리자 경로 제외
                path.startsWith("/api/music")                // 음악 관련 경로 제외
        ) {
            log.info("{} 경로는 filter 적용x", path);     // true == Filter 적용 제외
            return true;
        }

        log.info("{} 경로는 filter 적용o", path);         // false == Filter 적용
        return false;
    }

    // 필터 적용
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("----------------------------");

        String tokenHeader = request.getHeader("Authorization");
        log.info("tokenHeader: {}", tokenHeader);

        try {
            String accessToken = tokenHeader.substring(7); // "Bearer "를 제거한 토큰 가져옴
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);

            log.info("JWT claims: {}", claims);
            request.setAttribute("claims", claims);
            filterChain.doFilter(request, response);

        } catch (RuntimeException e) {
            log.info("JWT Check RuntimeException Error: {}", e.getMessage());

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            if ("MalformedJwtException".equals(e.getMessage())) {
                response.getWriter().write("{\"message\": \"JWT 토큰의 형식이 올바르지 않습니다. 토큰 값을 확인해 주세요.\"}");
            } else if ("ExpiredJwtException".equals(e.getMessage())) {
                response.getWriter().write("{\"message\": \"JWT 토큰이 만료되었습니다. 다시 로그인 해주세요.\"}");
            }

        } catch (Exception e) {
            log.info("JWT Check Exception Error: {}", e.getMessage());

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"JWT 토큰 기타 에러가 발생하였습니다. 로그아웃 후 다시 로그인 해주세요.\"}");
        }
    }
}
