package com.nova.narrativa.domain.llm.entity;


import com.nova.narrativa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId; // 게임 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User와 연관 관계 (1:N)

    @Column(nullable = false)
    private String genre; // 게임 장르

    @Column(length = 1000)
    private String initialStory; // 초기 스토리

    @Lob
    @Column(name = "prompt")
    private String prompt; // 프롬프트

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stage> stages = new ArrayList<>(); // 스테이지 리스트
}