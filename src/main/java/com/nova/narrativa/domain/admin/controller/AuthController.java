package com.nova.narrativa.domain.admin.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.dto.TokenRequest;
import com.nova.narrativa.domain.admin.dto.TokenResponse;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AdminService;
import com.nova.narrativa.domain.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${environments.narrativa-admin.url}",
        allowCredentials = "true",
        allowedHeaders = "*",
        exposedHeaders = "*")
public class AuthController {
    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TokenRequest request) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(request.getIdToken());
            String email = decodedToken.getEmail();
            String uid = decodedToken.getUid();

            AdminUser user = authService.findOrCreateUser(uid, email, decodedToken.getName());
            adminService.updateLastLoginAt(user.getId());

            return ResponseEntity.ok(
                    new TokenResponse(
                            user.getUid(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.getUsername()
                    )
            );
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody TokenRequest request) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(request.getIdToken());
            AdminUser adminUser = authService.registerAdminUser(decodedToken);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("관리자 등록 요청이 성공적으로 접수되었습니다: " + adminUser.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Firebase 토큰 검증 실패.");
        }
    }
}