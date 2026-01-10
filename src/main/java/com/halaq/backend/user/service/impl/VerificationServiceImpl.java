package com.halaq.backend.user.service.impl;

import com.halaq.backend.user.dto.IdCardDataDto;
import com.halaq.backend.user.service.facade.VerificationService;
import com.halaq.backend.user.service.mindee.MindeeService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VerificationServiceImpl implements VerificationService {

    private final MindeeService mindeeService;

    public VerificationServiceImpl(MindeeService mindeeService) {
        this.mindeeService = mindeeService;
    }
    @Override
    public IdCardDataDto extractIdCardData(MultipartFile file) throws Exception {

        // 1. Appeler votre MindeeService pour obtenir les données brutes
        MindeeService.DocumentInfo docInfo = mindeeService.processInternationalId(file);

        // 2. Mapper l'objet DocumentInfo vers votre IdCardDataDto
        return toIdCardDataDto(docInfo);
    }

    /**
     * Méthode privée pour mapper l'objet interne DocumentInfo de MindeeService
     * vers le DTO public IdCardDataDto.
     */
    private IdCardDataDto toIdCardDataDto(MindeeService.DocumentInfo docInfo) {
        if (docInfo == null) {
            return null;
        }

        return IdCardDataDto.builder()
                .documentNumber(docInfo.getDocumentNumber())
                .givenNames(docInfo.getGivenNames())
                .surnames(docInfo.getSurnames())
                .birthDate(docInfo.getBirthDate())
                .sex(docInfo.getSex())
                .nationality(docInfo.getNationality())
                .isExpired(docInfo.isExpired())
                .expiryDate(docInfo.getExpiryDate())
                .confidence(docInfo.getOverallConfidence())
                .build();
    }
}