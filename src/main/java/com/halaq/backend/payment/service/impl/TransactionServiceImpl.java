package com.halaq.backend.payment.service.impl;

import com.halaq.backend.payment.criteria.TransactionCriteria;
import com.halaq.backend.payment.entity.Transaction;
import com.halaq.backend.payment.repository.TransactionRepository;
import com.halaq.backend.payment.service.facade.TransactionService;
import com.halaq.backend.payment.specification.TransactionSpecification;
import com.halaq.backend.shared.TransactionStatus;
import com.halaq.backend.core.service.AbstractServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class TransactionServiceImpl extends AbstractServiceImpl<Transaction, TransactionCriteria, TransactionRepository> implements TransactionService {

    public TransactionServiceImpl(TransactionRepository dao) {
        super(dao);
    }

    @Override
    public void configure() {
        super.configure(Transaction.class, TransactionSpecification.class);
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        log.info("Creating transaction for user: {}", transaction.getUser().getId());
        return dao.save(transaction);
    }

    @Override
    @Transactional
    public Transaction updateTransactionStatus(Long transactionId, TransactionStatus status) {
        log.info("Updating transaction status: {} to {}", transactionId, status);
        Transaction transaction = findById(transactionId);
        if (transaction != null) {
            transaction.setStatus(status);
            return dao.save(transaction);
        }
        return null;
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        log.info("Finding transactions for user: {}", userId);
        return dao.findByUserId(userId);
    }

    @Override
    public List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status) {
        log.info("Finding transactions for user: {} with status: {}", userId, status);
        return dao.findByUserIdAndStatus(userId, status);
    }

    @Override
    public BigDecimal getTotalAmountByUserAndStatus(Long userId, TransactionStatus status) {
        log.info("Calculating total amount for user: {} with status: {}", userId, status);
        BigDecimal total = dao.getTotalAmountByUserAndStatus(userId, status);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Transaction findByStripePaymentIntentId(String stripePaymentIntentId) {
        log.info("Finding transaction by Stripe payment intent ID: {}", stripePaymentIntentId);
        return dao.findByStripePaymentIntentId(stripePaymentIntentId).orElse(null);
    }
}

