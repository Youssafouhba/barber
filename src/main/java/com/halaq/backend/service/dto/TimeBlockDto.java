package com.halaq.backend.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.halaq.backend.core.dto.AuditBaseDto;
import com.halaq.backend.user.dto.BarberDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TimeBlockDto extends AuditBaseDto {
    @NotNull(message = "La date/heure de début est obligatoire.")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "UTC"
    )
    private LocalDateTime startDateTime;

    @NotNull(message = "La date/heure de fin est obligatoire.")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "UTC"
    )
    private LocalDateTime endDateTime;

    private String reason;

    @NotNull(message = "Le coiffeur est obligatoire.")
    private BarberDto barber;
}


