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

    private final String validApiKey;

    public JWTCheckFilter(String validApiKey) {
        this.validApiKey = validApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/users/sign-up")   ||  // 회원가입 제외
                path.startsWith("/login")           ||  // 소셜 로그인 제외
                path.startsWith("/actuator/health") ||  // health check 제외
                path.startsWith("/api/admin")       ||  // 관리자 관리 관련 모든 경로 제외
                path.startsWith("/api/auth")        ||  // 관리자 인증 관련 모든 경로 제외
                path.startsWith("/api/notices")     ||  // 알람 제외
                path.startsWith("/api/music")       ||  // 관리자 S3 관리 경로 제외
                path.startsWith("/api/prompts")     ||  // 관리자 프롬프트 편집 제외
                path.startsWith("/api/health")      ||  // 관리자 배포상태 체크 제외
                path.startsWith("/v3/api-docs")     ||  // 스웨거 명세서
                path.startsWith("/swagger-ui"))         // 스웨거 명세서
        {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String tokenHeader = request.getHeader("Authorization");
        String apiKey = request.getHeader("X-API-Key");

        try {
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                // JWT 검증
                String accessToken = tokenHeader.substring(7); // "Bearer " 제거
                Map<String, Object> claims = JWTUtil.validateToken(accessToken);
                request.setAttribute("claims", claims);
                filterChain.doFilter(request, response);
            } else if (apiKey != null && apiKey.equals(validApiKey)) {
                // API 키 인증
                filterChain.doFilter(request, response);
            } else {
                // 인증 실패 처리
                response.setContentType("application/json; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\": \"유효하지 않은 인증 정보입니다. Authorization 헤더 또는 X-API-Key를 확인해 주세요.\"}");
            }
        } catch (RuntimeException e) {
            log.info("JWT Check RuntimeException Error: {}", e.getMessage());
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            if (e.getMessage().equals("MalformedJwtException")) {
                response.getWriter().write("{\"message\": \"JWT 토큰의 형식이 올바르지 않습니다. 토큰 값을 확인해 주세요.\"}");
            } else if (e.getMessage().equals("ExpiredJwtException")) {
                response.getWriter().write("{\"message\": \"JWT 토큰이 만료되었습니다. 다시 로그인 해주세요.\"}");
            } else {
                response.getWriter().write("{\"message\": \"JWT 토큰 기타 에러가 발생하였습니다. 로그아웃 후 다시 로그인 해주세요.\"}");
            }
        } catch (Exception e) {
            log.info("JWT Check Exception Error: {}", e.getMessage());
            response.setContentType("application/json; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"인증 처리 중 알 수 없는 오류가 발생했습니다.\"}");
        }
    }
}
