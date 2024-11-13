package com.nova.narrativa.domain.game.entity;

import com.nova.narrativa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stories",
        indexes = {
                @Index(name = "idx_story_user_id", columnList = "user_id"),
                @Index(name = "idx_story_game_id", columnList = "game_id"),
                @Index(name = "idx_story_progress", columnList = "progress"),
                @Index(name = "idx_story_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "JSON")
    private String choices;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Progress progress = Progress.IN_PROGRESS;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Progress {
        IN_PROGRESS, COMPLETED
    }
}
