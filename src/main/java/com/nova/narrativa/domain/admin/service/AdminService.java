package com.nova.narrativa.domain.admin.service;

import com.nova.narrativa.domain.admin.dto.AdminResponse;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        List<AdminUser> admins = adminRepository.findAll();
        return admins.stream()
                .map(AdminResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminResponse updateStatus(Long userId, AdminUser.Status status) {
        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // SUPER_ADMIN 상태 변경 불가
        if (adminUser.getRole() == AdminUser.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN의 상태는 변경할 수 없습니다.");
        }

        adminUser.setStatus(status);
        AdminUser updatedUser = adminRepository.save(adminUser);
        return AdminResponse.from(updatedUser);
    }

    @Transactional
    public AdminUser updateAdminRole(Long userId, AdminUser.Role newRole) {
        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // SUPER_ADMIN으로 변경 불가
        if (newRole == AdminUser.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN 권한으로 변경할 수 없습니다.");
        }

        adminUser.setRole(newRole);
        return adminRepository.save(adminUser);
    }

    // currentUser를 포함한 기존 메서드는 유지
    @Transactional
    public AdminUser updateAdminRole(Long userId, AdminUser.Role newRole, AdminUser currentUser) {
        validateRoleChangePermission(userId, currentUser);
        return updateAdminRole(userId, newRole);
    }

    // 권한 변경 가능 여부 확인
    private void validateRoleChangePermission(Long targetUserId, AdminUser currentUser) {
        if (currentUser.getRole() != AdminUser.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        AdminUser targetUser = adminRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + targetUserId));
        if (targetUser.getRole() == AdminUser.Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN의 권한은 변경할 수 없습니다.");
        }
    }

    // 마지막 로그인 시간 업데이트
    @Transactional
    public void updateLastLoginAt(Long userId) {
        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        adminUser.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(adminUser);
    }

    @Transactional
    public AdminUser getAdminByUid(String uid) {
        return adminRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
    }
}

