package com.railway.tracking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "train_positions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long trainId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private Double speed;

    private String currentStation;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }
}
