package com.nova.narrativa.domain.admin.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.dto.TokenRequest;
import com.nova.narrativa.domain.admin.dto.TokenResponse;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AdminService;
import com.nova.narrativa.domain.admin.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final AdminService adminService;

    public AuthController(AuthService authService, AdminService adminService) {
        this.authService = authService;
        this.adminService = adminService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TokenRequest request) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(request.getIdToken());
            String email = decodedToken.getEmail();

            Optional<AdminUser> user = authService.findUserByEmail(email);

            if (user.isEmpty()) {
                // 등록되지 않은 사용자
                return ResponseEntity.ok(new TokenResponse(decodedToken.getUid(), email, null));
            }

            AdminUser adminUser = user.get();
            return ResponseEntity.ok(new TokenResponse(decodedToken.getUid(), email, adminUser.getRole().name()));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody TokenRequest request) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(request.getIdToken());
            AdminUser adminUser = authService.registerAdminUser(decodedToken);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("관리자가 성공적으로 등록되었습니다: " + adminUser.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firebase 토큰 검증 실패.");
        }
    }

    @PatchMapping("/activate/{userId}")
    public ResponseEntity<?> activateAdmin(@PathVariable Long userId, @RequestParam String role) {
        try {
            AdminUser.Role newRole = AdminUser.Role.valueOf(role.toUpperCase());
            // currentUser 파라미터 제거
            AdminUser updatedUser = adminService.updateAdminRole(userId, newRole);
            return ResponseEntity.ok("관리자 권한이 업데이트되었습니다: " + updatedUser.getRole());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

