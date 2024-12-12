package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.dashboard.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = {"${environments.narrativa-admin.url}", "${environments.narrativa-front.url}"}, allowCredentials = "true")
public class StatisticsController {

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