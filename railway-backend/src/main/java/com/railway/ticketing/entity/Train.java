package com.railway.ticketing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "trains")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String trainNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_station_id", nullable = false)
    private Station fromStation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_station_id", nullable = false)
    private Station toStation;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false)
    private String trainType; // EXPRESS, RAJDHANI, SHATABDI, LOCAL

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunningFrequency frequency;

    @ElementCollection
    @CollectionTable(name = "train_running_days", joinColumns = @JoinColumn(name = "train_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> runningDays;
}
