package com.halaq.backend.service.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.service.dto.ReviewDto;
import com.halaq.backend.service.entity.Review;
import com.halaq.backend.user.mapper.BarberMapper;
import com.halaq.backend.user.mapper.ClientMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper extends BaseMapper<Review, ReviewDto> {

}