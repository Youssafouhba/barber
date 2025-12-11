package com.halaq.backend.user.service.facade;

import com.halaq.backend.core.service.IService;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.dto.BarberProfileSetupDto;
import com.halaq.backend.user.dto.StepData;
import com.halaq.backend.user.entity.Barber;
import io.minio.errors.MinioException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BarberService extends IService<Barber, BarberCriteria> {




    // Example of a business-specific method
    Barber approveBarber(Long barberId);

    Barber updateStep2Data(Long barberId, StepData stepData);

    FileUploadResult uploadBarberAvatar(
            Long barberId,
            MultipartFile file);

    byte[] downloadBarberAvatar(Long barberId) throws MinioException;

    /**
     * Soumet le profil complet d'un barbier pour validation.
     * Met à jour les informations du profil et change son statut.
     * @param setupDto Les données complètes de l'onboarding.
     * @return Le barbier mis à jour.
     */
    Barber submitProfileForValidation(BarberProfileSetupDto setupDto);

    @Transactional
        // Assure que l'opération est atomique
    Barber updateAvailability(Long barberId, boolean newStatus);

    List<Booking> findActiveBookingsByBarberId(Long barberId);
}