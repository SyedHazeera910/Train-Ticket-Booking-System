package com.railway.ticketing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "Train ID is required")
    private Long trainId;
    
    @NotBlank(message = "Travel class is required")
    private String travelClass;
    
    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least one seat must be booked")
    @Max(value = 6, message = "Maximum 6 seats can be booked")
    private Integer numberOfSeats;
    
    @NotEmpty(message = "Passenger details are required")
    private List<String> passengers; // "Name|Age|Gender"
}
