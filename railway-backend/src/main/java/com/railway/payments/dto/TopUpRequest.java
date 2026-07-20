package com.railway.payments.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {
    @NotNull(message = "Amount is required for top up")
    @DecimalMin(value = "1.0", message = "Top up amount must be at least 1.0")
    private BigDecimal amount;
}
