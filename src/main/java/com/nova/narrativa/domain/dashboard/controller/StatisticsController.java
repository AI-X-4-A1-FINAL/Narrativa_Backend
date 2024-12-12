package com.nova.narrativa.domain.dashboard.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AuthService;
import com.nova.narrativa.domain.dashboard.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final AuthService authService;

    @GetMapping("/basic")
    public ResponseEntity<?> getBasicStats(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // 토큰 추출
            String idToken = extractToken(authorizationHeader);
            
            // 토큰 검증
            FirebaseToken decodedToken = authService.verifyToken(idToken);
            String uid = decodedToken.getUid();

            // 사용자 확인
            AdminUser adminUser = authService.findUserByUid(uid)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + uid));

            // 관리자 권한 확인 (WAITING이 아닌 모든 권한 허용)
            if (adminUser.getRole() == AdminUser.Role.WAITING) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("승인 대기 중인 관리자는 접근할 수 없습니다.");
            }

            // 통계 데이터 반환
            Map<String, Object> stats = statisticsService.getBasicStats();
            return ResponseEntity.ok(stats);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Authorization 헤더가 유효하지 않습니다.");
    }

    @PostMapping("/increment-traffic")
    public ResponseEntity<Void> incrementTraffic() {
        statisticsService.incrementVisitCount();
        return ResponseEntity.ok().build();
    }
}