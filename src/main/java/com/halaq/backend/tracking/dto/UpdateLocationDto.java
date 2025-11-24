package com.halaq.backend.tracking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLocationDto {
    private double latitude;
    private double longitude;
}