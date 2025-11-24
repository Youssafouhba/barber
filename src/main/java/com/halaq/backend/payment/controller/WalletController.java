package com.halaq.backend.payment.controller;

import com.halaq.backend.payment.converter.WalletConverter;
import com.halaq.backend.payment.criteria.WalletCriteria;
import com.halaq.backend.payment.dto.WalletDto;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.payment.service.facade.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Wallets")
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletConverter walletConverter;

    public WalletController(WalletService walletService, WalletConverter walletConverter) {
        this.walletService = walletService;
        this.walletConverter = walletConverter;
    }

    @Operation(summary = "Finds all wallets")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<WalletDto>> findByCriteria(@RequestBody WalletCriteria criteria) throws Exception {
        List<Wallet> wallets = walletService.findByCriteria(criteria);
        List<WalletDto> dtos = walletConverter.toDto(wallets);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Finds a wallet by ID")
    @GetMapping("/{id}")
    public ResponseEntity<WalletDto> findById(@PathVariable Long id) {
        Wallet wallet = walletService.findById(id);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Finds a wallet by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDto> findByUserId(@PathVariable Long userId) {
        Wallet wallet = walletService.findByUserId(userId);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Creates a wallet for a user")
    @PostMapping("/user/{userId}")
    public ResponseEntity<WalletDto> createWalletForUser(@PathVariable Long userId) {
        Wallet wallet = walletService.createWalletForUser(userId);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Adds balance to wallet")
    @PostMapping("/user/{userId}/add-balance")
    public ResponseEntity<WalletDto> addBalance(
            @PathVariable Long userId,
            @RequestBody BigDecimal amount) {
        Wallet wallet = walletService.addBalance(userId, amount);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Deducts balance from wallet")
    @PostMapping("/user/{userId}/deduct-balance")
    public ResponseEntity<WalletDto> deductBalance(
            @PathVariable Long userId,
            @RequestBody BigDecimal amount) {
        Wallet wallet = walletService.deductBalance(userId, amount);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Checks if user has sufficient balance")
    @GetMapping("/user/{userId}/check-balance")
    public ResponseEntity<Boolean> hasSufficientBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount) {
        boolean hasBalance = walletService.hasSufficientBalance(userId, amount);
        return ResponseEntity.ok(hasBalance);
    }

    @Operation(summary = "Gets wallet balance for a user")
    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }
}

