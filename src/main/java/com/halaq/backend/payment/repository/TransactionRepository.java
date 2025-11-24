package com.halaq.backend.payment.repository;

import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.shared.TransactionType;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends AbstractRepository<Transaction, Long> {

    /**
     * Finds all transactions for a specific user
     * @param userId The user ID
     * @return List of transactions
     */
    List<Transaction> findByUserId(Long userId);

    /**
     * Finds all transactions by status
     * @param status The transaction status
     * @return List of transactions
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Finds all transactions by type
     * @param type The transaction type
     * @return List of transactions
     */
    List<Transaction> findByType(TransactionType type);

    /**
     * Finds a transaction by Stripe payment intent ID
     * @param stripePaymentIntentId The Stripe payment intent ID
     * @return An Optional containing the transaction if found
     */
    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Finds transactions for a user by status
     * @param userId The user ID
     * @param status The transaction status
     * @return List of transactions
     */
    List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status);

    /**
     * Calculates the total amount of transactions for a user by status
     * @param userId The user ID
     * @param status The transaction status
     * @return Total amount
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.status = :status")
    BigDecimal getTotalAmountByUserAndStatus(@Param("userId") Long userId, @Param("status") TransactionStatus status);
}

