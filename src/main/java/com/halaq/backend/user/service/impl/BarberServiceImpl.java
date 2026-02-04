package com.halaq.backend.user.service.impl;

import com.halaq.backend.core.exception.EntityNotFoundException;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.core.security.repository.facade.core.VerificationTrackerRepository;
import com.halaq.backend.core.service.AbstractServiceImpl;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.transverse.cloud.sevice.facade.MinIOService;
import com.halaq.backend.core.util.FileStorageService;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.repository.OfferedServiceRepository;
import com.halaq.backend.user.converter.ServiceZoneConverter;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.dto.*;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.repository.BarberRepository;
import com.halaq.backend.user.service.facade.BarberLiveLocationsService; // IMPORT AJOUTÉ
import com.halaq.backend.user.service.facade.BarberService;
import com.halaq.backend.user.service.facade.ServiceZoneService;
import com.halaq.backend.user.specification.BarberSpecification;
import io.minio.errors.MinioException;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;
import static com.halaq.backend.core.util.FileUtils.generateUniqueFileName;

@Service
public class BarberServiceImpl extends AbstractServiceImpl<Barber, BarberCriteria, BarberRepository> implements BarberService {
    private static final Logger log = LoggerFactory.getLogger(BarberServiceImpl.class);

    private final MinIOService minioService;
    private final ServiceZoneConverter serviceZoneConverter;
    private final ServiceZoneService serviceZoneService;
    private final VerificationTrackerRepository verificationTrackerRepository;
    private final FileStorageService fileStorageService;
    private final OfferedServiceRepository offeredServiceRepository;
    // INJECTION DU SERVICE REDIS
    private final BarberLiveLocationsService barberLiveLocationsService;

    public BarberServiceImpl(BarberRepository dao,
                             MinIOService minioService,
                             ServiceZoneConverter serviceZoneConverter,
                             ServiceZoneService serviceZoneService,
                             VerificationTrackerRepository verificationTrackerRepository,
                             FileStorageService fileStorageService,
                             OfferedServiceRepository offeredServiceRepository,
                             BarberLiveLocationsService barberLiveLocationsService) { // AJOUTÉ AU CONSTRUCTEUR
        super(dao);
        this.minioService = minioService;
        this.serviceZoneConverter = serviceZoneConverter;
        this.serviceZoneService = serviceZoneService;
        this.verificationTrackerRepository = verificationTrackerRepository;
        this.fileStorageService = fileStorageService;
        this.offeredServiceRepository = offeredServiceRepository;
        this.barberLiveLocationsService = barberLiveLocationsService; // ASSIGNATION
    }

    @Override
    public void configure() {
        super.configure(Barber.class, BarberSpecification.class);
    }

    @Override
    public Barber update(Barber barber) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur n'est authentifié. Impossible de soumettre le profil.");
        }
        barber.setId(currentUser.getId());
        Barber barber1 = findById(currentUser.getId());
        barber1.setAbout(barber.getAbout());
        return super.update(barber1);
    }

    /**
     * Mettre à jour les zones d'intervention
     */
    @Override
    public Barber updateStep2Data(Long barberId, StepData stepData) {
        // 1. Charger le Barber
        Barber barber = findWithAssociatedLists(barberId);
        if (barber == null) {
            throw new RuntimeException("Barber not found");
        }

        // 2. Mettre à jour les champs simples
        barber.setLocation(stepData.getLocation());
        barber.setBarberShopName(stepData.getBarberShopName());

        // 3. Convertir les DTOs en entités
        List<ServiceZone> newZones = serviceZoneConverter.toItem(stepData.getZones());

        for (ServiceZone zone : newZones) {
            zone.setBarber(barber);
        }

        // 4. Mettre la nouvelle liste sur l'entité
        barber.setZones(newZones);

        // 5. Update
        Barber updatedBarber = super.update(barber);

        // 6. Mettre à jour le tracker
        VerificationTracker tracker = verificationTrackerRepository.findByUserId(barberId)
                .orElseThrow(() -> new RuntimeException("Verification tracker not found for barber id: " + barberId));
        tracker.setServiceZoneSetup(true);
        tracker.setServiceZoneSetupAt(LocalDateTime.now());
        verificationTrackerRepository.save(tracker);

        return updatedBarber;
    }

    /**
     * Upload l'avatar d'un barbier
     */
    @Override
    public FileUploadResult uploadBarberAvatar(Long barberId, MultipartFile file) {
        String uniqueName = generateUniqueFileName(barberId, file.getOriginalFilename());
        Barber barber = dao.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barbier non trouvé"));

        if (barber.getAvatar() != null && !barber.getAvatar().isEmpty()) {
            String oldObjectName = barber.getAvatar();
            minioService.deleteFile(oldObjectName,"barber-avatars");
        }

            Optional<FileUploadResult> uploadResult = minioService.uploadFile(file, "barber-avatars", uniqueName);

        if (!uploadResult.isPresent()) {
            throw new RuntimeException("Erreur lors de l'upload du fichier");
        }

        FileUploadResult result = uploadResult.get();
        barber.setAvatar(uniqueName);
        dao.save(barber);
        return result;
    }

    /**
     * Télécharger l'avatar d'un barbier
     */
    @Override
    public byte[] downloadBarberAvatar(Long barberId) throws MinioException {
        Barber barber = dao.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barbier non trouvé"));

        if (barber.getAvatar() == null || barber.getAvatar().isEmpty()) {
            throw new RuntimeException("Aucun avatar trouvé");
        }

        return fileStorageService.downloadFile("barber-avatars", barber.getAvatar());
    }

    private String extractObjectNameFromUrl(String avatarUrl) {
        return avatarUrl.substring(avatarUrl.lastIndexOf("barbers/") + 8);
    }

    @Override
    @Transactional
    public Barber submitProfileForValidation(BarberProfileSetupDto setupDto) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur n'est authentifié. Impossible de soumettre le profil.");
        }

        Barber barberToUpdate = dao.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profil barbier non trouvé pour l'utilisateur actuel."));

        BarberDto barberDataFromDto = setupDto.getBarber();
        if (barberDataFromDto != null) {
            barberToUpdate.setYearsOfExperience(barberDataFromDto.getYearsOfExperience());
            barberToUpdate.setLocation(barberDataFromDto.getLocation());
            barberToUpdate.setBarberShopName(barberDataFromDto.getBarberShopName());
        }

        IdCardDataDto idCardData = setupDto.getIdCardData();
        if (idCardData != null) {
            barberToUpdate.setFirstName(idCardData.getGivenNames().get(0));
            barberToUpdate.setLastName(idCardData.getSurnames().get(0));
            barberToUpdate.setCin(idCardData.getDocumentNumber());
            barberToUpdate.setAge(new LocalDate().getYear() - idCardData.getBirthDate().getYear());
        }

        barberToUpdate.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
        return dao.save(barberToUpdate);
    }

    @Override
    @Transactional
    public Barber create(Barber barber) {
        barber.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
        barber.setEnabled(false);
        return super.create(barber);
    }

    @Override
    @Transactional
    public Barber approveBarber(Long barberId) {
        Barber barber = findById(barberId);
        if (barber != null && barber.getStatus() == AccountStatus.PENDING_EMAIL_VERIFICATION) {
            barber.setStatus(AccountStatus.ACTIVE);
            barber.setEnabled(true);
            return dao.save(barber);
        }
        return null;
    }

    /**
     * Met à jour le statut de disponibilité d'un barbier.
     * Si le barbier se met hors ligne, on supprime sa localisation de Redis.
     */
    @Transactional
    @Override
    public Barber updateAvailability(Long barberId, boolean newStatus) {

        // 1. Trouver le barbier
        Barber barber = dao.findById(barberId)
                .orElseThrow(() -> new EntityNotFoundException("Barber not found with id: " + barberId));

        log.info("Updating barber availability for barber id: " + barberId);

        // 2. Mettre à jour son statut (Toggle ou utilisation de newStatus si tu préfères)
        // Ici je respecte ta logique de "toggle" (!getIsAvailable)
        // Mais si tu veux utiliser le paramètre 'newStatus', change la ligne ci-dessous par : barber.setIsAvailable(newStatus);
        boolean nextState = !barber.getIsAvailable();
        barber.setIsAvailable(nextState);

        log.info("Barber availability updated to: " + nextState);

        // 3. GESTION REDIS
        // Si le nouveau statut est "Hors ligne" (false), on supprime la localisation de Redis
        if (!nextState) {
            try {
                barberLiveLocationsService.removeBarberLocation(barberId);
                log.info("Location removed from Redis for barber id: " + barberId);
            } catch (Exception e) {
                log.error("Failed to remove location from Redis", e);
                // On ne bloque pas la transaction DB si Redis échoue, mais on log l'erreur
            }
        }

        // 4. Sauvegarder dans PostgreSQL
        return dao.save(barber);
    }

    @Override
    public List<Booking> findActiveBookingsByBarberId(Long barberId) {
        return dao.findActiveBookingsByBarberId(barberId);
    }
}