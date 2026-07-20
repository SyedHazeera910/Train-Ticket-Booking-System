package com.railway.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data @AllArgsConstructor @NoArgsConstructor
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private String type;
    private String description;
    private Instant createdAt;
}
