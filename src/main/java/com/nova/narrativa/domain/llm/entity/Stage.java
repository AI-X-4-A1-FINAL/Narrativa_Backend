package com.nova.narrativa.domain.llm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageId; // 스테이지 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game; // Game과 연관 관계 (N:1)

    @Column(nullable = false)
    private int stageNumber; // 스테이지 번호

    @Column
    private String userChoice; // 사용자가 선택한 내용

    @Lob
    @Column(name = "image_url", columnDefinition = "LONGBLOB")
    private byte[] imageUrl;

    @Lob
    private String choices; // 스토리 선택지

    @Column
    private LocalDateTime startTime; // 스테이지 시작 시간

    @Column
    private LocalDateTime endTime; // 스테이지 종료 시간

    @Column
    private String story; //

    @Column
    private Integer probability;
}