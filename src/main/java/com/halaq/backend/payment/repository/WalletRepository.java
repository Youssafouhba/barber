package com.halaq.backend.payment.repository;

import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends AbstractRepository<Wallet, Long> {

    /**
     * Finds a wallet by user ID
     * @param userId The user ID
     * @return An Optional containing the wallet if found
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Checks if a wallet exists for a user
     * @param userId The user ID
     * @return true if wallet exists, false otherwise
     */
    boolean existsByUserId(Long userId);
}

