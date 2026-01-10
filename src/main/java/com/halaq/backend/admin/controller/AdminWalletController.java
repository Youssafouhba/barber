package com.halaq.backend.admin.controller;

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

/**
 * Admin controller for managing all wallets in the system.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - Wallet Management")
@RestController
@RequestMapping("/api/admin/wallets")
public class AdminWalletController {

    private final WalletService walletService;
    private final WalletConverter walletConverter;

    public AdminWalletController(WalletService walletService, WalletConverter walletConverter) {
        this.walletService = walletService;
        this.walletConverter = walletConverter;
    }

    @Operation(summary = "Get all wallets (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<WalletDto>> findByCriteria(@RequestBody WalletCriteria criteria) throws Exception {
        List<Wallet> wallets = walletService.findByCriteria(criteria);
        List<WalletDto> dtos = walletConverter.toDto(wallets);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get wallet by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<WalletDto> findById(@PathVariable Long id) {
        Wallet wallet = walletService.findById(id);
        if (wallet != null) {
            return ResponseEntity.ok(walletConverter.toDto(wallet));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get wallet by user ID (Admin only)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDto> findByUserId(@PathVariable Long userId) {
        Wallet wallet = walletService.findByUserId(userId);
        if (wallet != null) {
            return ResponseEntity.ok(walletConverter.toDto(wallet));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a wallet for a user (Admin only)")
    @PostMapping("/user/{userId}")
    public ResponseEntity<WalletDto> createWalletForUser(@PathVariable Long userId) {
        Wallet wallet = walletService.createWalletForUser(userId);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Add balance to wallet (Admin only)")
    @PostMapping("/user/{userId}/add-balance")
    public ResponseEntity<WalletDto> addBalance(
            @PathVariable Long userId,
            @RequestBody BigDecimal amount) {
        Wallet wallet = walletService.addBalance(userId, amount);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Deduct balance from wallet (Admin only)")
    @PostMapping("/user/{userId}/deduct-balance")
    public ResponseEntity<WalletDto> deductBalance(
            @PathVariable Long userId,
            @RequestBody BigDecimal amount) {
        Wallet wallet = walletService.deductBalance(userId, amount);
        return ResponseEntity.ok(walletConverter.toDto(wallet));
    }

    @Operation(summary = "Get wallet balance for a user (Admin only)")
    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Get wallet statistics (Admin only)")
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        // TODO: Implement statistics in AdminDashboardService
        // This could include total wallets, total balance, average balance, etc.
        return ResponseEntity.ok().build();
    }
}

