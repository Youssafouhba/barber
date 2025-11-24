package com.halaq.backend.payment.service.impl;

import com.halaq.backend.payment.criteria.WalletCriteria;
import com.halaq.backend.payment.entity.Wallet;
import com.halaq.backend.payment.repository.WalletRepository;
import com.halaq.backend.payment.service.facade.WalletService;
import com.halaq.backend.payment.specification.WalletSpecification;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.service.facade.UserService;
import com.halaq.backend.core.exception.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class WalletServiceImpl extends AbstractServiceImpl<Wallet, WalletCriteria, WalletRepository> implements WalletService {

    @Autowired
    private UserService userService;

    public WalletServiceImpl(WalletRepository dao) {
        super(dao);
    }

    @Override
    public void configure() {
        super.configure(Wallet.class, WalletSpecification.class);
    }

    @Override
    @Transactional
    public Wallet createWalletForUser(Long userId) {
        log.info("Creating wallet for user: {}", userId);
        User user = userService.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        // Check if wallet already exists
        Wallet existingWallet = dao.findByUserId(userId).orElse(null);
        if (existingWallet != null) {
            log.warn("Wallet already exists for user: {}", userId);
            return existingWallet;
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("MAD");
        return dao.save(wallet);
    }

    @Override
    public Wallet findByUserId(Long userId) {
        log.info("Finding wallet for user: {}", userId);
        return dao.findByUserId(userId).orElse(null);
    }

    @Override
    @Transactional
    public Wallet addBalance(Long userId, BigDecimal amount) {
        log.info("Adding balance to wallet for user: {}, amount: {}", userId, amount);
        Wallet wallet = findByUserId(userId);
        if (wallet == null) {
            wallet = createWalletForUser(userId);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        return dao.save(wallet);
    }

    @Override
    @Transactional
    public Wallet deductBalance(Long userId, BigDecimal amount) {
        log.info("Deducting balance from wallet for user: {}, amount: {}", userId, amount);
        Wallet wallet = findByUserId(userId);
        if (wallet == null) {
            throw new EntityNotFoundException("Wallet not found for user: " + userId);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        return dao.save(wallet);
    }

    @Override
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        log.info("Checking balance for user: {}, required amount: {}", userId, amount);
        Wallet wallet = findByUserId(userId);
        if (wallet == null) {
            return false;
        }
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        log.info("Getting balance for user: {}", userId);
        Wallet wallet = findByUserId(userId);
        if (wallet == null) {
            return BigDecimal.ZERO;
        }
        return wallet.getBalance();
    }


}

