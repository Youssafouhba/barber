package com.halaq.backend.service.service.facade;

import com.halaq.backend.core.service.IService;
import com.halaq.backend.service.criteria.TimeBlockCriteria;
import com.halaq.backend.service.entity.TimeBlock;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeBlockService extends IService<TimeBlock, TimeBlockCriteria> {

    List<TimeBlock> findByBarberId(Long barberId);

    List<TimeBlock> findByBarberIdAndBetween(Long barberId, LocalDateTime start, LocalDateTime end);
}


