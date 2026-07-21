package com.railway.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long id;
    private String pnr;
    private String trainName;
    private String trainNumber;
    private String fromStation;
    private String toStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String travelClass;
    private Integer numberOfSeats;
    private List<String> passengers;
    private String status;
    private BigDecimal fare;
    private Instant createdAt;
    private LocalDate travelDate;
}
