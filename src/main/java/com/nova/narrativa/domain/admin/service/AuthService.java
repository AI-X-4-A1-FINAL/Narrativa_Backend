package com.nova.narrativa.domain.admin.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AdminRepository adminRepository;

    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(idToken);
    }

    @Transactional
    public AdminUser findOrCreateUser(String uid, String email, String name) {
        // 1. UID로 먼저 검색
        Optional<AdminUser> userByUid = adminRepository.findByUid(uid);
        if (userByUid.isPresent()) {
            AdminUser user = userByUid.get();
            // WAITING 상태인 경우 접근 거부
            if (user.getRole() == AdminUser.Role.WAITING) {
                throw new IllegalStateException("승인 대기 중인 사용자입니다. 관리자 승인이 필요합니다.");
            }
            return user;
        }

        // 2. 이메일로 검색
        Optional<AdminUser> userByEmail = adminRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            AdminUser existingUser = userByEmail.get();
            // WAITING 상태인 경우 접근 거부
            if (existingUser.getRole() == AdminUser.Role.WAITING) {
                throw new IllegalStateException("승인 대기 중인 사용자입니다. 관리자 승인이 필요합니다.");
            }
            existingUser.setUid(uid);
            return adminRepository.save(existingUser);
        }

        // 3. 신규 사용자 등록 - 별도의 회원가입 프로세스로 리다이렉트
        throw new IllegalStateException("등록되지 않은 사용자입니다. 회원가입이 필요합니다.");
    }

    @Transactional
    public AdminUser registerAdminUser(FirebaseToken token) {
        String email = token.getEmail();
        String username = token.getName();
        String uid = token.getUid();

        // UID와 이메일 모두로 중복 체크
        if (adminRepository.findByUid(uid).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 사용자입니다. (UID: " + uid + ")");
        }

        if (adminRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다: " + email);
        }

        AdminUser newAdmin = AdminUser.builder()
                .uid(uid)
                .email(email)
                .username(username != null ? username : "Unknown User")
                .role(AdminUser.Role.WAITING)  // 기본 권한은 WAITING
                .status(AdminUser.Status.ACTIVE)
                .build();

        return adminRepository.save(newAdmin);
    }

    public Optional<AdminUser> findUserByUid(String uid) {
        return adminRepository.findByUid(uid);
    }
}
