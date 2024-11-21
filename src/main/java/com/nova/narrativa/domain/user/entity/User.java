package com.nova.narrativa.domain.user.entity;

import com.nova.narrativa.domain.game.entity.Story;
import com.nova.narrativa.domain.game.entity.UserGame;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_status", columnList = "status"),
                @Index(name = "idx_user_role", columnList = "role"),
                @Index(name = "idx_user_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"stories", "userGames"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private String userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 255)
    private String profile_url;     // null 이면 front에서 기본 이미지 띄우고, 있으면 해당 url 띄우기

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ROLE_USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginType loginType;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Story> stories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGame> userGames = new ArrayList<>();

    public enum LoginType {
        KAKAO, GOOGLE, GITHUB
    }

    public enum Role {
        ROLE_USER, ROLE_VIP
    }

    public enum Status {
        ACTIVE, INACTIVE, BANNED
    }
}
