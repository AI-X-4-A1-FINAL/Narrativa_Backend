package com.nova.narrativa.domain.notice.controller;

import com.nova.narrativa.domain.notice.dto.NoticeDTO;
import com.nova.narrativa.domain.notice.entity.Notice;
import com.nova.narrativa.domain.notice.service.NoticeService;
import com.nova.narrativa.domain.admin.entity.AdminUser;
import com.nova.narrativa.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final AdminService adminService;

    // 공지사항 목록 조회
    @GetMapping
    public ResponseEntity<List<NoticeDTO.Response>> getNotices() {
        List<Notice> notices = noticeService.getNotices();
        List<NoticeDTO.Response> responseList = notices.stream()
                .map(NoticeDTO.Response::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    // 공지사항 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO.Response> getNotice(@PathVariable Long id) {
        Notice notice = noticeService.getNotice(id);
        return ResponseEntity.ok(new NoticeDTO.Response(notice));
    }

    // 공지사항 생성
    @PostMapping
    public ResponseEntity<?> createNotice(
            @RequestBody NoticeDTO.CreateRequest request,
            @RequestHeader("Firebase-Token") String firebaseUid) {
        try {
            AdminUser currentAdmin = adminService.getAdminByUid(firebaseUid);
            Notice newNotice = noticeService.createNotice(
                    request.getTitle(), request.getContent(), currentAdmin
            );
            return ResponseEntity.ok(new NoticeDTO.Response(newNotice));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long id,
            @RequestBody NoticeDTO.UpdateRequest request,
            @RequestHeader("Firebase-Token") String firebaseUid) {
        try {
            AdminUser currentAdmin = adminService.getAdminByUid(firebaseUid);
            Notice updatedNotice = noticeService.updateNotice(
                    id, request.getTitle(), request.getContent(), currentAdmin
            );
            return ResponseEntity.ok(new NoticeDTO.Response(updatedNotice));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // 공지사항 삭제 (소프트 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable Long id,
            @RequestHeader("Firebase-Token") String firebaseUid) {
        try {
            AdminUser currentAdmin = adminService.getAdminByUid(firebaseUid);
            noticeService.deleteNotice(id, currentAdmin);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}