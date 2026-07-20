package com.railway.payments.service;

import com.railway.identity.entity.User;
import com.railway.identity.repository.UserRepository;
import com.railway.payments.dto.*;
import com.railway.payments.entity.Transaction;
import com.railway.payments.entity.Wallet;
import com.railway.payments.repository.TransactionRepository;
import com.railway.payments.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public WalletResponse getWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        List<TransactionDto> txns = transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
        return new WalletResponse(wallet.getId(), wallet.getBalance(), txns);
    }

    @Transactional
    public WalletResponse topUp(String email, BigDecimal amount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction txn = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(Transaction.TransactionType.CREDIT)
                .description("Wallet top-up")
                .build();
        transactionRepository.save(txn);

        return getWallet(email);
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getAmount(), t.getType().name(),
                t.getDescription(), t.getCreatedAt());
    }
}
