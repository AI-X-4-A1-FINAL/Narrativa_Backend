package com.nova.narrativa.domain.notice.repository;

import com.nova.narrativa.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findAllByStatusOrderByCreatedAtDesc(Notice.Status status);
}