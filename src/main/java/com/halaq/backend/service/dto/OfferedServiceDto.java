package com.halaq.backend.service.dto;

import com.halaq.backend.core.dto.AuditBaseDto;
import com.halaq.backend.user.dto.BarberDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OfferedServiceDto extends AuditBaseDto {
    @NotBlank(message = "Le nom du service ne peut pas être vide.")
    private String name;

    private String description;

    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0.")
    private BigDecimal price;

    @NotNull(message = "La durée est obligatoire.")
    @Min(value = 5, message = "La durée doit être d'au moins 5 minutes.")
    private Integer durationInMinutes;

    @NotNull(message = "La barber est obligatoire.")
    private BarberDto barber;

    @NotNull(message = "La catégorie de service est obligatoire.")
    private ServiceCategoryDto category;
}