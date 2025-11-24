package com.halaq.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarberLiveLocationsDto {
    private Long barberId;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private Float heading;
    private Float speed;
    private Long currentBookingId;
    private String timestamp;
    private String lastUpdate;

}
