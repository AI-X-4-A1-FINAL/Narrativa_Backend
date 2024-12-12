package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.dashboard.dto.TargetHealthResponse;
import com.nova.narrativa.domain.dashboard.service.TargetGroupHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/health")
public class HealthCheckController {

    private final TargetGroupHealthService targetGroupHealthService;

    @GetMapping("/target-groups")
    public ResponseEntity<Map<String, List<TargetHealthResponse>>> getAllTargetGroupsHealth() {
        return ResponseEntity.ok(targetGroupHealthService.getAllTargetGroupsHealth());
    }
}
