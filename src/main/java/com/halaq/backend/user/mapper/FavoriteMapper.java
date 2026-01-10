package com.halaq.backend.user.mapper;

import com.halaq.backend.core.mapper.BaseMapper;
import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.user.dto.FavoriteDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, BarberMapper.class})
public interface FavoriteMapper extends BaseMapper<Favorite, FavoriteDto> {
}