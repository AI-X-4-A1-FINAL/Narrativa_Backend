package com.nova.narrativa.domain.admin.service;

import com.nova.narrativa.domain.admin.dto.AdminResponse;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    // 모든 관리자 조회
    public List<AdminResponse> getAllAdmins() {
        List<AdminUser> admins = adminRepository.findAll();
        return admins.stream()
                .map(AdminResponse::from)
                .collect(Collectors.toList());
    }

    // 관리자 상태 업데이트
    public AdminResponse updateStatus(Long userId, AdminUser.Status status) {
        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        adminUser.setStatus(status);
        AdminUser updatedUser = adminRepository.save(adminUser);
        return AdminResponse.from(updatedUser);
    }

    // 관리자 권한 업데이트
    public AdminUser updateAdminRole(Long userId, AdminUser.Role newRole, AdminUser currentUser) {
        validateRoleChangePermission(userId, currentUser);

        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        adminUser.setRole(newRole);
        return adminRepository.save(adminUser);
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
    public void updateLastLoginAt(Long userId) {
        AdminUser adminUser = adminRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        adminUser.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(adminUser);
    }

    public AdminUser getAdminByUid(String uid) {
        return adminRepository.findByUid(uid)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
    }
}

