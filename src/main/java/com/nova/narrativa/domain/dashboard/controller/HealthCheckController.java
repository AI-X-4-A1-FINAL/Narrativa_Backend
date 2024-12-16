package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.dashboard.service.TargetGroupHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {
    private final TargetGroupHealthService targetGroupHealthService;

    public HealthCheckController(TargetGroupHealthService targetGroupHealthService) {
        this.targetGroupHealthService = targetGroupHealthService;
    }

    @AdminAuth
    @GetMapping("/target-groups")
    public ResponseEntity<?> getAllTargetGroupsHealth() {
        return ResponseEntity.ok(targetGroupHealthService.getAllTargetGroupsHealth());
    }
}