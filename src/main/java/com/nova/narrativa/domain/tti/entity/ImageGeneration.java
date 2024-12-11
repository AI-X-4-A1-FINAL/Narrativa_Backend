package com.nova.narrativa.domain.tti.entity;

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
        name = "image_generations",
        indexes = {
                @Index(name = "idx_image_generation_style", columnList = "style"),
                @Index(name = "idx_image_generation_status", columnList = "status"),
                @Index(name = "idx_image_generation_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Style style;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false, unique = true, length = 255)
    private String s3Url;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Style {
        MYSTERY, SURVIVAL, ROMANCE, SIMULATION
    }

    public enum Status {
        PENDING, COMPLETED, FAILED
    }
}