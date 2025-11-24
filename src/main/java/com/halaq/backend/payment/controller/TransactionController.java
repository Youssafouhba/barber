package com.halaq.backend.payment.controller;

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

@Tag(name = "Transactions")
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionConverter transactionConverter;

    public TransactionController(TransactionService transactionService, TransactionConverter transactionConverter) {
        this.transactionService = transactionService;
        this.transactionConverter = transactionConverter;
    }

    @Operation(summary = "Finds all transactions")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<TransactionDto>> findByCriteria(@RequestBody TransactionCriteria criteria) throws Exception {
        List<Transaction> transactions = transactionService.findByCriteria(criteria);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Finds a transaction by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> findById(@PathVariable Long id) {
        Transaction transaction = transactionService.findById(id);
        return ResponseEntity.ok(transactionConverter.toDto(transaction));
    }

    @Operation(summary = "Finds all transactions for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDto>> findByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.findByUserId(userId);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Finds transactions by status for a user")
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<TransactionDto>> findByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable TransactionStatus status) {
        List<Transaction> transactions = transactionService.findByUserIdAndStatus(userId, status);
        List<TransactionDto> dtos = transactionConverter.toDto(transactions);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Creates a new transaction")
    @PostMapping
    public ResponseEntity<TransactionDto> save(@RequestBody TransactionDto dto) throws Exception {
        Transaction transaction = transactionConverter.toItem(dto);
        Transaction saved = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(transactionConverter.toDto(saved));
    }

    @Operation(summary = "Updates transaction status")
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDto> updateStatus(
            @PathVariable Long id,
            @RequestBody TransactionStatus status) {
        Transaction transaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(transactionConverter.toDto(transaction));
    }
}

