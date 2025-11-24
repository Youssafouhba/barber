package com.halaq.backend.payment.service.facade;

import com.halaq.backend.payment.criteria.WalletCriteria;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.core.service.IService;

import java.math.BigDecimal;

public interface WalletService extends IService<Wallet, WalletCriteria> {
    
    /**
     * Creates a wallet for a user
     * @param userId The user ID
     * @return The created wallet
     */
    Wallet createWalletForUser(Long userId);
    
    /**
     * Finds a wallet by user ID
     * @param userId The user ID
     * @return The wallet if found
     */
    Wallet findByUserId(Long userId);
    
    /**
     * Adds amount to wallet balance
     * @param userId The user ID
     * @param amount The amount to add
     * @return The updated wallet
     */
    Wallet addBalance(Long userId, BigDecimal amount);
    
    /**
     * Deducts amount from wallet balance
     * @param userId The user ID
     * @param amount The amount to deduct
     * @return The updated wallet
     */
    Wallet deductBalance(Long userId, BigDecimal amount);
    
    /**
     * Checks if user has sufficient balance
     * @param userId The user ID
     * @param amount The amount to check
     * @return true if balance is sufficient
     */
    boolean hasSufficientBalance(Long userId, BigDecimal amount);
    
    /**
     * Gets wallet balance for a user
     * @param userId The user ID
     * @return The balance
     */
    BigDecimal getBalance(Long userId);
}

