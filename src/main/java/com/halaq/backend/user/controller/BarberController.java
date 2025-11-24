package com.halaq.backend.user.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.core.exception.EntityNotFoundException;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.core.util.PaginatedList;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.user.converter.BarberConverter;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.dto.AvailabilityStatusDto;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.dto.BarberProfileSetupDto;
import com.halaq.backend.user.dto.StepData;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.service.facade.BarberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Tag(name = "Barber Management")
@RestController
@RequestMapping("/api/v1/barbers")
public class BarberController extends AbstractController<Barber, BarberDto, BarberCriteria, BarberService, BarberConverter> {

    private static final Logger logger = LoggerFactory.getLogger(BarberController.class);

    public BarberController(BarberService service, BarberConverter converter) {
        super(service, converter);
    }


    /**
     * Find all barbers
     */
    @Operation(summary = "Get all barbers")
    @GetMapping
    public ResponseEntity<List<BarberDto>> getAllBarbers() throws Exception {
        List<Barber> barbers = service.findByCriteria(null);
        return ResponseEntity.ok(converter.toCustomDto(barbers, Booking.class, Document.class));
    }

    /**
     * Find Paginated Barbers
     */
    @Operation(summary = "Get paginated barbers")
    @PostMapping("/find-paginated-by-criteria")
    public ResponseEntity<PaginatedList> findPaginatedByCriteria(@RequestBody BarberCriteria criteria) throws Exception {
        criteria.setIsAvailable(true);
        return super.findPaginatedByCriteria(criteria);
    }

    /**
     * Endpoint pour que le barbier change son statut En Ligne / Hors Ligne.
     */
    @PostMapping("/status")
    public ResponseEntity<BarberDto> updateBarberStatus(
            @RequestBody AvailabilityStatusDto statusDTO) {
        User authenticatedUser = getCurrentUser();
        // 1. Vérifie que l'utilisateur est bien connecté
        if (authenticatedUser == null) {
            return ResponseEntity.status(401).build(); // Non autorisé
        }

        // 2. On utilise l'ID de l'utilisateur authentifié (le token JWT)
        // Un barbier ne peut pas changer le statut d'un autre barbier.
        try {
            return ResponseEntity.ok().body(converter.toDto(service.updateAvailability(authenticatedUser.getId(), statusDTO.isAvailable())));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).build(); // Barbier non trouvé
        }
    }

    /**
     * Mettre à jour les zones d'un barbier (Step 2)
     */
    @PutMapping("/step2-data/{barberId}")
    public ResponseEntity<BarberDto> updateBarberZones(
            @PathVariable Long barberId,
            @Valid @RequestBody StepData stepData) {
        try {
            logger.info("[BarberController] Mise à jour Step2 pour barbier: {}", barberId);
            Barber updatedBarber = service.updateStep2Data(barberId, stepData);
            logger.info("[BarberController] Step2 mise à jour avec succès");
            return ResponseEntity.ok(converter.toDto(updatedBarber));
        } catch (IllegalArgumentException e) {
            logger.error("[BarberController] Erreur validation Step2: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la mise à jour Step2", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }



    /**
     * Mettre à jour un barber
     */
    @Operation(summary = "Update a barber")
    @PutMapping
    public ResponseEntity<BarberDto> update(@Valid @RequestBody BarberDto dto) {
        try {
            logger.info("[BarberController] Mise à jour barbier");
            Barber updatedBarber = service.update(converter.toItem(dto));
            logger.info("[BarberController] Barbier mis à jour avec succès");
            return new ResponseEntity<BarberDto>(converter.toDto(updatedBarber), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la mise à jour", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Upload avatar pour un barbier
     * Supprime l'ancien avatar automatiquement
     */
    @PostMapping("/{barberId}/avatar/upload")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long barberId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            logger.warn("[BarberController] Avatar upload - File is empty");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("File cannot be empty", "400"));
        }

        try {
            logger.info("[BarberController] Avatar upload pour barbier: {}", barberId);
            FileUploadResult result = service.uploadBarberAvatar(barberId, file);
            logger.info("[BarberController] Avatar uploadé avec succès");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("[BarberController] Barber not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Barber not found", "404"));
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de l'upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    /**
     * Télécharger l'avatar d'un barbier
     */
    @GetMapping("/{barberId}/avatar/download")
    public ResponseEntity<?> downloadAvatar(@PathVariable Long barberId) {
        try {
            logger.info("[BarberController] Téléchargement avatar pour barbier: {}", barberId);
            byte[] fileContent = service.downloadBarberAvatar(barberId);

            logger.info("[BarberController] Avatar téléchargé avec succès");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar.jpg\"")
                    .body(fileContent);
        } catch (IllegalArgumentException e) {
            logger.warn("[BarberController] Avatar not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Avatar not found", "404"));
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors du téléchargement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    @Operation(summary = "Find a barber by ID")
    @GetMapping("/id/{id}")
    public ResponseEntity<BarberDto> findById(@PathVariable Long id) {
        try {
            logger.info("[BarberController] Recherche barbier ID: {}", id);
            Barber barber = service.findById(id);
            return ResponseEntity.ok(converter.toCustomDto(barber, Booking.class));
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la recherche", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Find a barber with associated lists by ID")
    @GetMapping("/id/{id}/with-lists")
    public ResponseEntity<BarberDto> findByIdWithAssociatedLists(@PathVariable Long id) {
        try {
            logger.info("[BarberController] Recherche barbier ID avec listes: {}", id);
            return super.findWithAssociatedLists(id);
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la recherche avec listes", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Note: Barber approval has been moved to AdminBarberController
    // Only admins can approve barbers via /api/admin/barbers/{id}/approve

    @Operation(summary = "Submits the barber's completed onboarding profile for validation")
    @PostMapping("/submit-profile-setup")
    public ResponseEntity<?> submitProfileSetup(@Valid @RequestBody BarberProfileSetupDto setupDto) {
        try {
            logger.info("[BarberController] Soumission profil setup pour barbier");
            Barber updatedBarber = service.submitProfileForValidation(setupDto);
            logger.info("[BarberController] Profil soumis avec succès");

            return ResponseEntity.ok(converter.toDto(updatedBarber));
        } catch (IllegalArgumentException e) {
            logger.warn("[BarberController] Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage(), "400"));
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la soumission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    /**
     * Helper pour créer une réponse d'erreur structurée
     */
    public static Map<String, Object> createErrorResponse(String message, String code) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("code", code);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}