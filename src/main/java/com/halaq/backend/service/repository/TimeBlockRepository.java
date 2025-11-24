package com.halaq.backend.service.repository;

import com.halaq.backend.core.repository.AbstractRepository;
import com.halaq.backend.service.entity.TimeBlock;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeBlockRepository extends AbstractRepository<TimeBlock, Long> {

    List<TimeBlock> findByBarberId(Long barberId);

    List<TimeBlock> findByBarberIdAndStartDateTimeBetween(Long barberId, LocalDateTime start, LocalDateTime end);

    List<TimeBlock> findByBarberIdAndEndDateTimeBetween(Long barberId, LocalDateTime start, LocalDateTime end);
}


