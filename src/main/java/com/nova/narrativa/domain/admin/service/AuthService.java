package com.nova.narrativa.domain.admin.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        // Firebase 토큰 검증
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }

    public AdminUser findOrCreateUser(String uid, String email, String name) {
        // UID로 관리자 조회
        Optional<AdminUser> optionalUser = adminRepository.findByUid(uid);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            // 등록되지 않은 사용자, WAITING 상태로 생성
            AdminUser newUser = new AdminUser();
            newUser.setUid(uid);
            newUser.setEmail(email);
            newUser.setUsername(name != null ? name : "Unknown User");
            newUser.setRole(AdminUser.Role.WAITING); // 기본 권한 WAITING
            newUser.setStatus(AdminUser.Status.ACTIVE);
            return adminRepository.save(newUser);
        }
    }

    public AdminUser registerAdminUser(FirebaseToken token) {
        String email = token.getEmail();
        String username = token.getName();
        String uid = token.getUid();

        // 이미 등록된 사용자 확인
        Optional<AdminUser> existingUser = adminRepository.findByUid(uid);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("이미 등록된 사용자입니다: " + email);
        }

        // 새 관리자 생성
        AdminUser newAdmin = new AdminUser();
        newAdmin.setUid(uid);
        newAdmin.setEmail(email);
        newAdmin.setUsername(username != null ? username : "Unknown User");
        newAdmin.setRole(AdminUser.Role.WAITING); // 기본 권한 WAITING
        newAdmin.setStatus(AdminUser.Status.ACTIVE);

        return adminRepository.save(newAdmin);
    }

    public Optional<AdminUser> findUserByUid(String uid) {
        return adminRepository.findByUid(uid);
    }
}
