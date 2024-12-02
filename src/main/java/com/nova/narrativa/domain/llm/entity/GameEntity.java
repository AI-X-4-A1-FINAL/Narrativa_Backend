package com.nova.narrativa.domain.llm.entity;

import com.nova.narrativa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "game_entity")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String genre;
    private int currentStage;

    @Column(columnDefinition = "TEXT")
    private String initialStory;

    @Column(columnDefinition = "TEXT")
    private String userSelect;

    @Column(columnDefinition = "TEXT")
    private String previousUserInput;

    @ElementCollection
    @CollectionTable(
            name = "conversation_history",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "message", columnDefinition = "TEXT")
    private List<String> ConversationHistory;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}