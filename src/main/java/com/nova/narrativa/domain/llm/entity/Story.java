package com.nova.narrativa.domain.llm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storyId;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private Integer stageNumber;

    @Column(columnDefinition = "TEXT")
    private String previousChoice;

    private String userChoice;

    private int choiceCount;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String conversationHistory;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
