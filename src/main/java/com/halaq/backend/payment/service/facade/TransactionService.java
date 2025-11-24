package com.halaq.backend.payment.service.facade;

import com.halaq.backend.payment.criteria.TransactionCriteria;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.core.service.IService;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService extends IService<Transaction, TransactionCriteria> {
    
    /**
     * Creates a new transaction
     * @param transaction The transaction to create
     * @return The created transaction
     */
    Transaction createTransaction(Transaction transaction);
    
    /**
     * Updates a transaction's status
     * @param transactionId The transaction ID
     * @param status The new status
     * @return The updated transaction
     */
    Transaction updateTransactionStatus(Long transactionId, TransactionStatus status);
    
    /**
     * Finds all transactions for a user
     * @param userId The user ID
     * @return List of transactions
     */
    List<Transaction> findByUserId(Long userId);
    
    /**
     * Finds transactions by status for a user
     * @param userId The user ID
     * @param status The transaction status
     * @return List of transactions
     */
    List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status);
    
    /**
     * Calculates total amount for user transactions by status
     * @param userId The user ID
     * @param status The transaction status
     * @return Total amount
     */
    BigDecimal getTotalAmountByUserAndStatus(Long userId, TransactionStatus status);
    
    /**
     * Finds a transaction by Stripe payment intent ID
     * @param stripePaymentIntentId The Stripe payment intent ID
     * @return The transaction if found
     */
    Transaction findByStripePaymentIntentId(String stripePaymentIntentId);
}

