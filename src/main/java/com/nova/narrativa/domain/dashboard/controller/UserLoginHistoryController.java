package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.dashboard.service.UserLoginHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class UserLoginHistoryController {
    private final UserLoginHistoryService loginHistoryService;

    public UserLoginHistoryController(UserLoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @AdminAuth
    @GetMapping("/active-users")
    public ResponseEntity<?> getActiveUsers() {
        return ResponseEntity.ok(loginHistoryService.getActiveUsersStats());
    }
}