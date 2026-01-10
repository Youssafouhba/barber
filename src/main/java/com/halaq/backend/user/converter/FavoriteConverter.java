package com.halaq.backend.user.converter;

import com.halaq.backend.core.mapper.context.CycleAvoidingMappingContext;
import com.halaq.backend.user.dto.FavoriteDto;
import com.halaq.backend.user.entity.Favorite;
import com.halaq.backend.user.mapper.FavoriteMapper;
import com.halaq.backend.core.converter.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class FavoriteConverter extends AbstractConverter<Favorite, FavoriteDto> {

    private final FavoriteMapper favoriteMapper;

    public FavoriteConverter(FavoriteMapper favoriteMapper) {
        super(Favorite.class, FavoriteDto.class);
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public Favorite toItem(FavoriteDto dto) {
        return dto == null ? null : favoriteMapper.toEntity(dto,new CycleAvoidingMappingContext());
    }

    @Override
    public FavoriteDto toDto(Favorite item) {
        return item == null ? null : favoriteMapper.toDto(item, new CycleAvoidingMappingContext());
    }

    @Override
    protected FavoriteDto mapToDtoInternal(Favorite item, CycleAvoidingMappingContext context) {
        return favoriteMapper.toDto(item, context);
    }
}