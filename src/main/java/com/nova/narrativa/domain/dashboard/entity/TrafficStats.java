package com.nova.narrativa.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrafficStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Long visitCount;

    @Builder
    public TrafficStats(LocalDateTime timestamp, Long visitCount) {
        this.timestamp = timestamp;
        this.visitCount = visitCount;
    }
}
