package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.dashboard.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = {"${environments.narrativa-admin.url}", "${environments.narrativa-client.url}"}, allowCredentials = "true")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AuthService authService;

    public StatisticsController(StatisticsService statisticsService, AuthService authService) {
        this.statisticsService = statisticsService;
        this.authService = authService;
    }

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

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @AdminAuth
    @GetMapping("/basic")
    public ResponseEntity<?> getBasicStats() {
        Map<String, Object> stats = statisticsService.getBasicStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/increment-traffic")
    public ResponseEntity<Void> incrementTraffic() {
        statisticsService.incrementVisitCount();
        return ResponseEntity.ok().build();
    }
}