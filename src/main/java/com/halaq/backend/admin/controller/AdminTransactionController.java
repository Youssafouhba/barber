package com.halaq.backend.admin.controller;

import com.halaq.backend.payment.converter.TransactionConverter;
import com.halaq.backend.payment.criteria.TransactionCriteria;
import com.halaq.backend.payment.dto.TransactionDto;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.payment.service.facade.TransactionService;
import com.halaq.backend.shared.TransactionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing all transactions in the system.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - Transaction Management")
@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionController {

    private final TransactionService transactionService;
    private final TransactionConverter transactionConverter;

    public AdminTransactionController(TransactionService transactionService, TransactionConverter transactionConverter) {
        this.transactionService = transactionService;
        this.transactionConverter = transactionConverter;
    }

    @Operation(summary = "Get all transactions (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<TransactionDto>> findByCriteria(@RequestBody TransactionCriteria criteria) throws Exception {
        List<Transaction> transactions = transactionService.findByCriteria(criteria);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get transaction by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> findById(@PathVariable Long id) {
        Transaction transaction = transactionService.findById(id);
        if (transaction != null) {
            return ResponseEntity.ok(transactionConverter.toDto(transaction));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get all transactions for a user (Admin only)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDto>> findByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.findByUserId(userId);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get transactions by status (Admin only)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionDto>> findByStatus(@PathVariable TransactionStatus status) {
        // TODO: Implement findByStatus in TransactionService if needed
        // For now, using criteria
        TransactionCriteria criteria = new TransactionCriteria();
        criteria.setStatus(status);
        List<Transaction> transactions = transactionService.findByCriteria(criteria);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get transactions by status for a user (Admin only)")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<TransactionDto>> findByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable TransactionStatus status) {
        List<Transaction> transactions = transactionService.findByUserIdAndStatus(userId, status);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Update transaction status (Admin only)")
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDto> updateStatus(
            @PathVariable Long id,
            @RequestBody TransactionStatus status) {
        Transaction transaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(transactionConverter.toDto(transaction));
    }

    @Operation(summary = "Create a new transaction (Admin only)")
    @PostMapping
    public ResponseEntity<TransactionDto> create(@RequestBody TransactionDto dto) throws Exception {
        Transaction transaction = transactionConverter.toItem(dto);
        Transaction saved = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(transactionConverter.toDto(saved));
    }

    @Operation(summary = "Get transaction statistics (Admin only)")
    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        // TODO: Implement statistics in AdminDashboardService
        // This could include total transactions, revenue, pending transactions, etc.
        return ResponseEntity.ok().build();
    }
}

