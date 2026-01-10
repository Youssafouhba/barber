package com.halaq.backend.user.service.facade;

import com.halaq.backend.user.dto.IdCardDataDto;
import org.springframework.web.multipart.MultipartFile;

public interface VerificationService {
    /**
     * Traite une image de CNI, extrait les données et les mappe vers un DTO.
     *
     * @param file Le fichier CNI uploadé
     * @return un IdCardDataDto contenant les informations extraites.
     * @throws Exception en cas d'erreur de traitement ou d'IO.
     */
    IdCardDataDto extractIdCardData(MultipartFile file) throws Exception;
}