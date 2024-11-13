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
@Table(name = "music_generations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    private String style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(nullable = true)
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
