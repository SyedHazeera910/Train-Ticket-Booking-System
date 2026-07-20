package com.railway.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class WalletResponse {
    private Long walletId;
    private BigDecimal balance;
    private List<TransactionDto> transactions;
}
