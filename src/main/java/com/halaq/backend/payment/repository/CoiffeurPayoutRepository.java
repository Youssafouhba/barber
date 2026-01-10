package com.halaq.backend.payment.repository;

import com.halaq.backend.payment.entity.CoiffeurPayout;
import com.halaq.backend.shared.PayoutStatus;
import com.halaq.backend.core.repository.AbstractRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoiffeurPayoutRepository extends AbstractRepository<CoiffeurPayout, Long> {
    
    List<CoiffeurPayout> findByStatus(PayoutStatus status);
    
    List<CoiffeurPayout> findByBarberIdAndStatusOrderByCreatedAtDesc(Long barberId, PayoutStatus status);
    
    @Query("SELECT cp FROM CoiffeurPayout cp WHERE cp.barber.id = :barberId AND cp.createdAt BETWEEN :startDate AND :endDate")
    List<CoiffeurPayout> findBarberPayoutHistory(
        @Param("barberId") Long barberId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}

