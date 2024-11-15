package com.nova.narrativa.domain.game.entity;

import com.nova.narrativa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_games",
        indexes = {
                @Index(name = "idx_user_game_user_id", columnList = "user_id"),
                @Index(name = "idx_user_game_game_id", columnList = "game_id"),
                @Index(name = "idx_user_game_user_game", columnList = "user_id, game_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
}
