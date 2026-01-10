package com.halaq.backend.user.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class BarberSearchResultDto {
    private Long id;
    private String fullName;
    private String avatar;
    private Double averageRating;
    // Le prix du service spécifique recherché par ce coiffeur
    private BigDecimal priceForService;
}