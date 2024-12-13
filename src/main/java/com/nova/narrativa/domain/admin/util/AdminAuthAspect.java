package com.nova.narrativa.domain.admin.util;

import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuthAspect {
    private final AuthService authService;

    @Around("@annotation(com.nova.narrativa.domain.admin.util.AdminAuth)")
    public Object authenticateAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorizationHeader = request.getHeader("Authorization");

        // OPTIONS 요청 처리
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return joinPoint.proceed();
        }

        try {
            // 토큰 추출 및 검증
            String idToken = extractToken(authorizationHeader);
            FirebaseToken decodedToken = authService.verifyToken(idToken);
            String uid = decodedToken.getUid();

            // 사용자 확인
            AdminUser adminUser = authService.findUserByUid(uid)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + uid));

            // 관리자 권한 확인 (SUPER_ADMIN과 SYSTEM_ADMIN만 허용)
            if (adminUser.getRole() != AdminUser.Role.SUPER_ADMIN &&
                    adminUser.getRole() != AdminUser.Role.SYSTEM_ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "해당 기능에 대한 접근 권한이 없습니다."));
            }

            // 인증 성공 시 원래 메소드 실행
            return joinPoint.proceed();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "인증에 실패했습니다."));
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Authorization 헤더가 유효하지 않습니다.");
    }
}