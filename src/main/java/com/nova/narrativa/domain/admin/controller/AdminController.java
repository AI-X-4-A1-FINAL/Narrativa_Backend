package com.nova.narrativa.domain.admin.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.dto.*;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AdminService;
import com.nova.narrativa.domain.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @Value("${environments.narrativa-admin.url}")
    private String narrativaAdminUrl;

    // 1. 모든 관리자 조회
    @GetMapping("/users")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    // 2. 관리자 상태 수정
    @PatchMapping("/users/{userId}/status")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<AdminResponse> updateAdminStatus(
            @PathVariable Long userId,
            @RequestBody UpdateStatusRequest request) {
        AdminResponse updatedAdmin = adminService.updateStatus(userId, request.getStatus());
        return ResponseEntity.ok(updatedAdmin);
    }

    // 3. 관리자 권한 수정
    @PatchMapping("/users/{userId}/role")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<?> updateAdminRole(
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = extractToken(authorizationHeader);
            FirebaseToken decodedToken = authService.verifyToken(idToken);
            String uid = decodedToken.getUid();

            // 현재 사용자 정보 가져오기
            AdminUser currentUser = authService.findUserByUid(uid)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + uid));

            AdminUser.Role newRole = request.getRole();
            AdminUser updatedUser = adminService.updateAdminRole(userId, newRole, currentUser);

            return ResponseEntity.ok("관리자 권한이 업데이트되었습니다: " + updatedUser.getRole());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 Firebase 토큰입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Authorization 헤더가 유효하지 않습니다.");
    }

    // 4. Firebase 토큰 검증 및 사용자 확인
    @PostMapping("/auth/verify")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    // 5. 관리자 등록 요청
    @PostMapping("/auth/register")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<?> registerAdmin(@RequestBody TokenRequest request) {
        try {
            FirebaseToken decodedToken = authService.verifyToken(request.getIdToken());
            AdminUser adminUser = authService.registerAdminUser(decodedToken);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("관리자 등록 요청이 성공적으로 접수되었습니다: " + adminUser.getEmail());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firebase 토큰 검증 실패.");
        }
    }
}