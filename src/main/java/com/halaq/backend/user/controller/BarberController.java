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
import com.halaq.backend.user.service.facade.BarberLiveLocationsService; // IMPORT AJOUTÉ
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

import java.util.*;
import java.util.stream.Collectors;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
@Tag(name = "Barber Management")
@RestController
@RequestMapping("/api/v1/barbers")
public class BarberController extends AbstractController<Barber, BarberDto, BarberCriteria, BarberService, BarberConverter> {

    private static final Logger logger = LoggerFactory.getLogger(BarberController.class);

    // Injection du service Redis
    private final BarberLiveLocationsService barberLiveLocationsService;
    private final SimpMessagingTemplate messagingTemplate;

    public BarberController(BarberService service, BarberConverter converter, BarberLiveLocationsService barberLiveLocationsService, SimpMessagingTemplate messagingTemplate) {
        super(service, converter);
        this.barberLiveLocationsService = barberLiveLocationsService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/live-location")
    public ResponseEntity<?> updateLiveLocation(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        // ✅ Récupération de l'utilisateur
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ Utilisateur non authentifié");
            return ResponseEntity.status(401).build();
        }

        Long barberId = currentUser.getId();
        System.out.println("📍 UPDATE LOCATION - Barber: " + barberId +
                " | Lat: " + latitude + " | Lng: " + longitude);

        try {
            // ✅ Sauvegarde en Redis
            barberLiveLocationsService.updateBarberLocation(barberId, latitude, longitude);
            System.out.println("✅ Redis UPDATED pour barber " + barberId);

            // ✅ Récupérer TOUTES les réservations actives de ce barbier
            List<Booking> activeBookings = service.findActiveBookingsByBarberId(barberId);

            if (activeBookings.isEmpty()) {
                System.out.println("⚠️ Aucune réservation active pour ce barbier");
            }

            // ✅ Envoi WebSocket vers TOUTES les réservations actives
            Map<String, Object> payload = Map.of(
                    "latitude", latitude,
                    "longitude", longitude,
                    "barberId", barberId,
                    "timestamp", System.currentTimeMillis()
            );

            List<String> destinations = new ArrayList<>();

            for (Booking booking : activeBookings) {
                // Envoyer à la fois vers le topic du barbier ET le topic de la réservation
                String barberDestination = "/topic/barber/" + barberId;
                String bookingDestination = "/topic/booking/" + booking.getId();
                String trackingDestination = "/topic/tracking/" + booking.getId();

                messagingTemplate.convertAndSend(barberDestination, payload);
                messagingTemplate.convertAndSend(bookingDestination, payload);
                messagingTemplate.convertAndSend(trackingDestination, payload); // AJOUT


                destinations.add(barberDestination);
                destinations.add(bookingDestination);

                System.out.println("🚀 PUSH WebSocket vers booking " + booking.getId());
            }

            System.out.println("✅ WebSocket messages envoyés: " + destinations);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Location updated",
                    "barberId", barberId,
                    "activeBookings", activeBookings.size(),
                    "destinations", destinations
            ));

        } catch (Exception e) {
            System.err.println("❌ ERREUR lors du push WebSocket: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Recherche des barbiers à proximité (Géolocalisation Redis + Données SQL)
     */
    @Operation(summary = "Find barbers nearby (Radius search)")
    @GetMapping("/search/nearby")
    public ResponseEntity<List<BarberDto>> findNearbyBarbers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10") double radius) {

        try {
            System.out.println("--- DÉBUT RECHERCHE ---");
            System.out.println("Recherche autour de: " + latitude + ", " + longitude + " Rayon: " + radius);

            // 1. Redis
            List<Long> nearbyBarberIds = barberLiveLocationsService.findNearbyBarberIds(latitude, longitude, radius);
            System.out.println("Redis a trouvé " + nearbyBarberIds.size() + " barbiers. IDs: " + nearbyBarberIds);

            if (nearbyBarberIds.isEmpty()) {
                System.out.println("-> Liste Redis vide. Arrêt.");
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Barber> resultBarbers = new ArrayList<>();
            for (Long id : nearbyBarberIds) {
                Barber b = service.findById(id);
                if (b == null) {
                    System.out.println("ID " + id + " trouvé dans Redis mais PAS dans PostgreSQL !");
                } else {
                    System.out.println("Barbier ID " + id + " trouvé. isAvailable = " + b.getIsAvailable());
                    if (Boolean.TRUE.equals(b.getIsAvailable())) {
                        resultBarbers.add(b);
                        System.out.println("-> Barbier AJOUTÉ aux résultats.");
                    } else {
                        System.out.println("-> Barbier REJETÉ (Non disponible).");
                    }
                }
            }

            System.out.println("--- FIN RECHERCHE : " + resultBarbers.size() + " résultats envoyés ---");
            return ResponseEntity.ok(converter.toCustomDto(resultBarbers, Booking.class, Document.class));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
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
        if (authenticatedUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Barber item = service.updateAvailability(authenticatedUser.getId(), statusDTO.isAvailable());
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).build();
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de la mise à jour Step2", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
     * Upload avatar
     */
    @PostMapping("/{barberId}/avatar/upload")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long barberId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("File cannot be empty", "400"));
        }

        try {
            FileUploadResult result = service.uploadBarberAvatar(barberId, file);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Barber not found", "404"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    /**
     * Download avatar
     */
    @GetMapping("/{barberId}/avatar/download")
    public ResponseEntity<?> downloadAvatar(@PathVariable Long barberId) {
        try {
            byte[] fileContent = service.downloadBarberAvatar(barberId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar.jpg\"")
                    .body(fileContent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Avatar not found", "404"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    @Operation(summary = "Find a barber by ID")
    @GetMapping("/id/{id}")
    public ResponseEntity<BarberDto> findById(@PathVariable Long id) {
        try {
            Barber barber = service.findById(id);
            return ResponseEntity.ok(converter.toCustomDto(barber, Booking.class));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Find a barber with associated lists by ID")
    @GetMapping("/id/{id}/with-lists")
    public ResponseEntity<BarberDto> findByIdWithAssociatedLists(@PathVariable Long id) {
        try {
            return super.findWithAssociatedLists(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Submits the barber's completed onboarding profile for validation")
    @PostMapping("/submit-profile-setup")
    public ResponseEntity<?> submitProfileSetup(@Valid @RequestBody BarberProfileSetupDto setupDto) {
        try {
            Barber updatedBarber = service.submitProfileForValidation(setupDto);
            return ResponseEntity.ok(converter.toDto(updatedBarber));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage(), "400"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    public static Map<String, Object> createErrorResponse(String message, String code) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("code", code);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}