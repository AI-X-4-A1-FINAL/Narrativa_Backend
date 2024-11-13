package com.nova.narrativa.domain.llm.entity;

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
        name = "llm_conversations",
        indexes = {
                @Index(name = "idx_llm_user_id", columnList = "user_id"),
                @Index(name = "idx_llm_status", columnList = "status"),
                @Index(name = "idx_llm_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LLMConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String inputText;

    @Column(columnDefinition = "TEXT")
    private String responseText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, COMPLETED, FAILED
    }
}
