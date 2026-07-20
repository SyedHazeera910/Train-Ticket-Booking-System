package com.railway.payments.controller;

import com.railway.payments.dto.WalletResponse;
import com.railway.payments.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import com.railway.payments.dto.TopUpRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(Authentication auth) {
        return ResponseEntity.ok(walletService.getWallet(auth.getName()));
    }

    @PostMapping("/topup")
    public ResponseEntity<WalletResponse> topUp(
            @Valid @RequestBody TopUpRequest request, Authentication auth) {
        return ResponseEntity.ok(walletService.topUp(auth.getName(), request.getAmount()));
    }
}
