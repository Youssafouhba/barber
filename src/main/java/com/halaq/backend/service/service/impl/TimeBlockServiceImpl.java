package com.halaq.backend.service.service.impl;

import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.service.criteria.TimeBlockCriteria;
import com.halaq.backend.service.entity.TimeBlock;
import com.halaq.backend.service.repository.TimeBlockRepository;
import com.halaq.backend.service.service.facade.TimeBlockService;
import com.halaq.backend.service.specification.TimeBlockSpecification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeBlockServiceImpl extends AbstractServiceImpl<TimeBlock, TimeBlockCriteria, TimeBlockRepository> implements TimeBlockService {

    public TimeBlockServiceImpl(TimeBlockRepository dao) {
        super(dao);
    }

    @Override
    public void configure() {
        super.configure(TimeBlock.class, TimeBlockSpecification.class);
    }

    @Override
    public List<TimeBlock> findByBarberId(Long barberId) {
        return dao.findByBarberId(barberId);
    }

    @Override
    public List<TimeBlock> findByBarberIdAndBetween(Long barberId, LocalDateTime start, LocalDateTime end) {
        // Combine overlapping queries as needed
        List<TimeBlock> byStart = dao.findByBarberIdAndStartDateTimeBetween(barberId, start, end);
        List<TimeBlock> byEnd = dao.findByBarberIdAndEndDateTimeBetween(barberId, start, end);
        byEnd.stream()
                .filter(tb -> byStart.stream().noneMatch(a -> a.getId().equals(tb.getId())))
                .forEach(byStart::add);
        return byStart;
    }
}


