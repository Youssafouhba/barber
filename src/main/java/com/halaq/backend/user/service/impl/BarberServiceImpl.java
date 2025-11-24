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
import com.halaq.backend.service.repository.OfferedServiceRepository;
import com.halaq.backend.user.converter.ServiceZoneConverter;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.dto.*;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.repository.BarberRepository;
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
import java.util.ArrayList;
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

    public BarberServiceImpl(BarberRepository dao, MinIOService minioService, ServiceZoneConverter serviceZoneConverter, ServiceZoneService serviceZoneService, VerificationTrackerRepository verificationTrackerRepository, FileStorageService fileStorageService, OfferedServiceRepository offeredServiceRepository) {
        super(dao);
        this.minioService = minioService;
        this.serviceZoneConverter = serviceZoneConverter;
        this.serviceZoneService = serviceZoneService;
        this.verificationTrackerRepository = verificationTrackerRepository;
        this.fileStorageService = fileStorageService;
        this.offeredServiceRepository = offeredServiceRepository;
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
        return super.update(barber);
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

        // 2. Mettre à jour les champs simples (sur l'entité chargée ou une copie si votre findWithAssociatedLists ne renvoie pas l'entité managée)
        barber.setLocation(stepData.getLocation());
        barber.setBarberShopName(stepData.getBarberShopName());

        // 3. Convertir les DTOs en entités, en s'assurant que la relation inverse est fixée
        List<ServiceZone> newZones = serviceZoneConverter.toItem(stepData.getZones());

        // ⭐ CORRECTION CLÉ 1 : Fixer la relation inverse (le parent) sur chaque enfant
        for (ServiceZone zone : newZones) {
            // Supposons que ServiceZone a un champ 'barber' ou 'owner'
            zone.setBarber(barber);
            // ⚠️ NOTE: Si ServiceZone n'a pas la relation inverse,
            // ce champ ne doit pas être défini ici, et la fusion doit être basée uniquement sur l'ID de la zone.
        }

        // 4. Mettre la nouvelle liste sur l'entité chargée (managée)
        // C'est dangereux si vous faites un 'Bean Copy' par la suite, mais nécessaire avant le super.update
        barber.setZones(newZones);

        // 5. Appeler le super.update() qui est censé gérer la fusion
        Barber updatedBarber = super.update(barber); // L'entité 'barber' est managée après l'étape 1

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
     * Supprime l'ancien avatar s'il existe
     */
    @Override
    public FileUploadResult uploadBarberAvatar(
        Long barberId,
        MultipartFile file) {
        String uniqueName = generateUniqueFileName(barberId, file.getOriginalFilename());
        // Recuperer le barbier
        Barber barber = dao.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barbier non trouvé"));

        // Supprimer l'ancien avatar s'il existe
        if (barber.getAvatar() != null && !barber.getAvatar().isEmpty()) {
            String oldObjectName = barber.getAvatar();
            minioService.deleteFile(oldObjectName,"barber-avatars");
        }

        // Upload le nouveau fichier
        Optional<FileUploadResult> uploadResult = minioService.uploadFile(file, "barber-avatars", uniqueName);

        if (!uploadResult.isPresent()) {
            throw new RuntimeException("Erreur lors de l'upload du fichier");
        }

        FileUploadResult result = uploadResult.get();

        // Mettre à jour l'URL de l'avatar dans la base de données
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




    /**
     * Extraire le nom de l'objet depuis l'URL
     * Ex: "https://minio/barbers/avatars/barber_1_123456.jpg" -> "avatars/barber_1_123456.jpg"
     */
    private String extractObjectNameFromUrl(String avatarUrl) {
        return avatarUrl.substring(avatarUrl.lastIndexOf("barbers/") + 8);
    }
    @Override
    @Transactional
    public Barber submitProfileForValidation(BarberProfileSetupDto setupDto) {
        // 1. Récupérer l'utilisateur actuellement authentifié. C'est crucial pour la sécurité.
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur n'est authentifié. Impossible de soumettre le profil.");
        }

        // 2. Récupérer le profil Barber associé à cet utilisateur.
        Barber barberToUpdate = dao.findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profil barbier non trouvé pour l'utilisateur actuel."));

        // 3. Mettre à jour les informations du barbier depuis le DTO.
        // On fusionne les données de l'onboarding avec l'entité existante.
        BarberDto barberDataFromDto = setupDto.getBarber();
        if (barberDataFromDto != null) {
            barberToUpdate.setYearsOfExperience(barberDataFromDto.getYearsOfExperience());
            barberToUpdate.setLocation(barberDataFromDto.getLocation());
            barberToUpdate.setBarberShopName(barberDataFromDto.getBarberShopName());
            // Ajoutez ici d'autres champs que vous collectez (bio, etc.)
        }

        // 4. Mettre à jour les informations extraites de la carte d'identité.
        IdCardDataDto idCardData = setupDto.getIdCardData();
        if (idCardData != null) {
            // Ces données confirment ou écrasent les informations initiales d'inscription.
            barberToUpdate.setFirstName(idCardData.getGivenNames().get(0));
            barberToUpdate.setLastName(idCardData.getSurnames().get(0));
            barberToUpdate.setCin(idCardData.getDocumentNumber());
            barberToUpdate.setAge(new LocalDate().getYear() - idCardData.getBirthDate().getYear());
        }

        // 5. Mettre à jour le statut du profil pour lancer le processus de validation.
        barberToUpdate.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
        // 6. Sauvegarder l'entité mise à jour et la retourner.
        return dao.save(barberToUpdate);
    }



    @Override
    @Transactional
    public Barber create(Barber barber) {
        // Business logic: New barbers start with a PENDING_VALIDATION status
        barber.setStatus(AccountStatus.PENDING_EMAIL_VERIFICATION);
        barber.setEnabled(false); // Can't log in until approved
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
        // Consider throwing a BusinessRuleException if barber is not found or already active
        return null;
    }

    /**
     * Met à jour le statut de disponibilité d'un barbier.
     *
     * @param barberId  L'ID du barbier à mettre à jour.
     * @param newStatus Le nouveau statut (true pour "En ligne", false pour "Hors ligne").
     * @return
     */
    @Transactional // Assure que l'opération est atomique
    @Override
    public Barber updateAvailability(Long barberId, boolean newStatus) {

        // 1. Trouver le barbier
        Barber barber = dao.findById(barberId)
                .orElseThrow(() -> new EntityNotFoundException("Barber not found with id: " + barberId));

        log.info("Updating barber availability for barber id: " + barberId);
        // 2. Mettre à jour son statut
        barber.setIsAvailable(!barber.getIsAvailable());
        log.info("Barber availability updated for barber status: " + !barber.getIsAvailable());

        // 3. Sauvegarder (géré par @Transactional, mais explicite c'est bien aussi)
        return super.update(barber);

        // (Logique future : vous pourriez aussi supprimer sa position de Redis ici s'il se déconnecte)
        // if (!newStatus) {
        //     redisTemplate.delete("barber:location:" + barberId);
        // }
    }
}