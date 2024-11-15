package com.nova.narrativa.domain.ttm.entity;

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
        name = "music_generations",
        indexes = {
                @Index(name = "idx_music_genre", columnList = "genre"),
                @Index(name = "idx_music_status", columnList = "status"),
                @Index(name = "idx_music_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genre genre;

    @Column(nullable = true, length = 100)
    private String style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = true, unique = true, length = 255)
    private String s3Url;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Genre {
        MYSTERY, SURVIVAL, ROMANCE, SIMULATION
    }

    public enum Status {
        PENDING, COMPLETED, FAILED
    }
}
