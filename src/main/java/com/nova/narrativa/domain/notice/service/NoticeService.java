package com.nova.narrativa.domain.notice.service;

import com.nova.narrativa.domain.notice.entity.Notice;
import com.nova.narrativa.domain.notice.repository.NoticeRepository;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 관리자 권한 확인
    private void validateAdminAccess(AdminUser adminUser) {
        if (adminUser.getStatus() != AdminUser.Status.ACTIVE) {
            throw new IllegalStateException("Inactive or suspended admin user cannot perform this action");
        }

        if (adminUser.getRole() == AdminUser.Role.WAITING) {
            throw new IllegalStateException("Waiting admin user cannot perform this action");
        }
    }

    // 공지사항 목록 조회 (활성화된 공지사항만)
    public List<Notice> getNotices() {
        return noticeRepository.findAllByStatusOrderByCreatedAtDesc(Notice.Status.ACTIVE);
    }

    // 공지사항 상세 조회
    public Notice getNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
    }

    // 공지사항 생성
    @Transactional
    public Notice createNotice(String title, String content, AdminUser currentAdmin) {
        validateAdminAccess(currentAdmin);

        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatedBy(currentAdmin);
        notice.setStatus(Notice.Status.ACTIVE);

        return noticeRepository.save(notice);
    }

    // 공지사항 수정
    @Transactional
    public Notice updateNotice(Long id, String title, String content, AdminUser currentAdmin) {
        Notice notice = getNotice(id);
        validateNoticeModifyAccess(notice, currentAdmin);

        notice.setTitle(title);
        notice.setContent(content);
        return noticeRepository.save(notice);
    }

    // 공지사항 소프트 삭제
    @Transactional
    public void deleteNotice(Long id, AdminUser currentAdmin) {
        Notice notice = getNotice(id);
        validateNoticeModifyAccess(notice, currentAdmin);

        notice.setStatus(Notice.Status.INACTIVE);
        noticeRepository.save(notice);
    }

    // 공지사항 수정 권한 확인
    private void validateNoticeModifyAccess(Notice notice, AdminUser adminUser) {
        validateAdminAccess(adminUser);

        // SUPER_ADMIN과 SYSTEM_ADMIN은 모든 공지사항 수정 가능
        if (adminUser.getRole() != AdminUser.Role.SUPER_ADMIN &&
                adminUser.getRole() != AdminUser.Role.SYSTEM_ADMIN) {

            // 다른 관리자는 자신이 작성한 공지사항만 수정 가능
            if (!notice.getCreatedBy().getId().equals(adminUser.getId())) {
                throw new IllegalStateException("You don't have permission to modify this notice");
            }
        }
    }
}