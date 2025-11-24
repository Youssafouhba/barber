package com.halaq.backend.service.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.service.dto.ReviewDto;
import com.halaq.backend.service.entity.Review;
import com.halaq.backend.service.mapper.ReviewMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ReviewConverter extends AbstractConverter<Review, ReviewDto> {

    private final ReviewMapper reviewMapper;

    public ReviewConverter(ReviewMapper reviewMapper) {
        super(Review.class, ReviewDto.class);
        this.reviewMapper = reviewMapper;
    }

    @Override
    public Review toItem(ReviewDto dto) {
        return dto == null ? null : reviewMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public ReviewDto toDto(Review item) {
        return item == null ? null : reviewMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected ReviewDto mapToDtoInternal(Review item, CycleAvoidingMappingContext context) {
        return reviewMapper.toDto(item, context);
    }
}