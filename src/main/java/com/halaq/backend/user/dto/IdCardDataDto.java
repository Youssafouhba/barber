package com.halaq.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class IdCardDataDto {

    // Champs principaux pour la vérification
    private String documentNumber; // cin
    private List<String> givenNames;
    private List<String> surnames;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String sex;
    private String nationality;

    // Métadonnées de validation
    private boolean isExpired;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    private double confidence;
}