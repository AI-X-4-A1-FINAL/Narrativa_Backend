package com.nova.narrativa.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_login_history",
        indexes = {
                @Index(name = "idx_login_date", columnList = "loginDate"),
                @Index(name = "idx_user_id", columnList = "userId")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate loginDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static UserLoginHistory createLoginHistory(String userId) {
        return UserLoginHistory.builder()
                .userId(userId)
                .loginDate(LocalDate.now())
                .build();
    }
}