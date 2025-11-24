package com.halaq.backend.user.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteDto extends AuditBaseDto {
    private ClientDto client;
    private BarberDto barber;
}