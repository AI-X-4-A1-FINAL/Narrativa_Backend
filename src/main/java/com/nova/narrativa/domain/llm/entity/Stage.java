package com.nova.narrativa.domain.llm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageId;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    private Integer stageNumber;

    @Column(columnDefinition = "TEXT")
    private String previousChoice;

    private String userChoice;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String conversationHistory;
}
