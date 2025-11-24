package com.halaq.backend.admin.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.core.controller.ErrorResponse;
import com.halaq.backend.core.security.controller.dto.UserInfo;
import com.halaq.backend.core.security.entity.VerificationTracker;
import com.halaq.backend.core.security.enums.AccountStatus;
import com.halaq.backend.core.security.service.facade.VerificationTrackerService;
import com.halaq.backend.core.util.PaginatedList;
import com.halaq.backend.shared.BarberStatus;
import com.halaq.backend.shared.dto.PaginatedResponse;
import com.halaq.backend.user.converter.BarberConverter;
import com.halaq.backend.user.criteria.BarberCriteria;
import com.halaq.backend.user.dto.BarberDto;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.repository.BarberRepository;
import com.halaq.backend.user.service.facade.BarberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller for managing barbers in the system.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - Barber Management")
@RestController
@RequestMapping("/api/v1/admin/barbers")
@Slf4j
public class AdminBarberController extends AbstractController<Barber, BarberDto, BarberCriteria, BarberService, BarberConverter> {

    private static final Logger logger = LoggerFactory.getLogger(AdminBarberController.class);
    private final BarberRepository barberRepository;
    private final VerificationTrackerService verificationTrackerService;

    public AdminBarberController(BarberService service, BarberConverter converter, BarberRepository barberRepository, VerificationTrackerService verificationTrackerService) {
        super(service, converter);
        this.barberRepository = barberRepository;
        this.verificationTrackerService = verificationTrackerService;
    }


    /**
     * ✅ CORRECTED: Get all barbers with pagination and filters
     * Now properly maps URL parameters to BarberCriteria
     */

    @Operation(summary = "Get all barbers with pagination and filters (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved barbers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            //@ApiResponse(responseCode = 401, description = "Unauthorized"),
            //@ApiResponse(responseCode = 403, description = "Forbidden - Admin access required")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<BarberDto>> getAllBarbers(
            @Parameter(description = "Page number (starts from 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "Sort order (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String order,

            @Parameter(description = "Sort field", example = "id")
            @RequestParam(defaultValue = "id") String sortField,

            @Parameter(description = "Search by full name")
            @RequestParam(required = false) String fullName,

            @Parameter(description = "Search by email")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filter by status (ACTIVE, PENDING, SUSPENDED, etc.)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filter by availability")
            @RequestParam(required = false) Boolean isAvailable,

            @Parameter(description = "Minimum years of experience")
            @RequestParam(required = false) Integer yearsOfExperienceMin,

            @Parameter(description = "Maximum years of experience")
            @RequestParam(required = false) Integer yearsOfExperienceMax,

            @Parameter(description = "Minimum average rating")
            @RequestParam(required = false) Double averageRatingMin,

            @Parameter(description = "Maximum average rating")
            @RequestParam(required = false) Double averageRatingMax) {

        try {
            log.info("[AdminBarberController] Fetching barbers - page: {}, limit: {}, filters: fullName: {}, email: {}, status: {}",
                    page, limit, fullName, email, status);

            // ✅ BUILD CRITERIA FROM REQUEST PARAMETERS
            BarberCriteria criteria = new BarberCriteria();
            criteria.setFullName(fullName);
            criteria.setEmail(email);
            criteria.setStatus(status != null ? AccountStatus.valueOf(status) : null);
            criteria.setIsAvailable(isAvailable);
            criteria.setYearsOfExperienceMin(yearsOfExperienceMin);
            criteria.setYearsOfExperienceMax(yearsOfExperienceMax);
            criteria.setAverageRatingMin(averageRatingMin);
            criteria.setAverageRatingMax(averageRatingMax);

            // Convert page from 1-indexed to 0-indexed for Spring Data
            int zeroBasedPage = page - 1;
            if (zeroBasedPage < 0) {
                zeroBasedPage = 0;
            }

            // ✅ CALL SERVICE WITH POPULATED CRITERIA
            List<Barber> barbers = service.findPaginatedByCriteria(criteria, zeroBasedPage, limit, order, sortField);
            int totalCount = service.getDataSize(criteria);

            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / limit);
            boolean hasNextPage = zeroBasedPage < totalPages - 1;
            boolean hasPreviousPage = zeroBasedPage > 0;



            // Convert to DTOs
            List<BarberDto> barberDtos = barbers.stream()
                    .map(converter::toDto)
                    .collect(Collectors.toList());
            // Build paginated response
            PaginatedResponse<BarberDto> response = PaginatedResponse.<BarberDto>builder()
                    .items(barberDtos)
                    .page(page) // Return 1-indexed page for frontend
                    .pageSize(limit)
                    .total(totalCount)
                    .totalPages(totalPages)
                    .hasNextPage(hasNextPage)
                    .hasPreviousPage(hasPreviousPage)
                    .build();

            log.info("[AdminBarberController] Successfully fetched {} barbers out of {} total",
                    barberDtos.size(), totalCount);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("[AdminBarberController] Invalid status parameter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[AdminBarberController] Error fetching barbers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search barbers
     */
    @Operation(summary = "Search barbers by name or email")
    @GetMapping("/search")
    public ResponseEntity<PaginatedResponse<BarberDto>> searchBarbers(
            @Parameter(description = "Search query")
            @RequestParam String q,

            @Parameter(description = "Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        try {
            log.info("[AdminBarberController] Searching barbers with query: '{}'", q);

            if (q == null || q.trim().isEmpty()) {
                log.warn("[AdminBarberController] Empty search query provided");
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(PaginatedResponse.<BarberDto>builder()
                                .items(List.of())
                                .page(page)
                                .pageSize(limit)
                                .total(0)
                                .totalPages(0)
                                .hasNextPage(false)
                                .hasPreviousPage(false)
                                .build());
            }

            // ✅ IMPORTANT: Add wildcard characters for LIKE search
            String searchQuery = "%" + q.trim() + "%";
            log.debug("[AdminBarberController] Search query with wildcards: '{}'", searchQuery);

            BarberCriteria criteria = new BarberCriteria();
            // Set BOTH fullName and email so specification uses OR logic
            criteria.setGlobalSearchQuery(searchQuery);

            int zeroBasedPage = Math.max(page - 1, 0);

            // Fetch barbers with criteria
            List<Barber> barbers = service.findPaginatedByCriteria(
                    criteria,
                    zeroBasedPage,
                    limit,
                    "asc",
                    "id"
            );

            // Get total count
            int totalCount = service.getDataSize(criteria);

            log.debug("[AdminBarberController] Search found {} barbers out of {} total",
                    barbers.size(), totalCount);

            if (barbers.isEmpty()) {
                log.warn("[AdminBarberController] No barbers found matching query: '{}'", q);
            }

            // Convert to DTOs
            List<BarberDto> barberDtos = barbers.stream()
                    .map(converter::toDto)
                    .collect(Collectors.toList());

            // Calculate pagination
            int totalPages = (int) Math.ceil((double) totalCount / limit);

            PaginatedResponse<BarberDto> response = PaginatedResponse.<BarberDto>builder()
                    .items(barberDtos)
                    .page(page)
                    .pageSize(limit)
                    .total(totalCount)
                    .totalPages(totalPages)
                    .hasNextPage(zeroBasedPage < totalPages - 1)
                    .hasPreviousPage(zeroBasedPage > 0)
                    .build();

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("[AdminBarberController] Error searching barbers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all barbers optimized (Admin only)")
    @GetMapping("/optimized")
    public ResponseEntity<List<BarberDto>> getAllBarbersOptimized() throws Exception {
        return super.findAllOptimized();
    }

    @Operation(summary = "Get barber by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<BarberDto> findById(@PathVariable Long id) {
        Barber barber = service.findById(id);
        ResponseEntity<BarberDto> res = null;
        if (barber != null) {
            converter.init(true);
            BarberDto barberDto = converter.toDto(barber);
            VerificationTracker tracker = verificationTrackerService.findByUserId(barberDto.getId()) .orElseGet(VerificationTracker::new);
            if(tracker.getId() != null) {
                barberDto.setUserInfo(UserInfo.builder()
                        .id(barberDto.getId())
                        .email(barberDto.getEmail())
                        .username(barberDto.getUsername())
                        .firstName(barberDto.getFirstName())
                        .lastName(barberDto.getLastName())
                        .fullName(barberDto.getFullName())
                        .phone(barberDto.getPhone())
                        .avatar(barberDto.getAvatar())
                        .accountCompletionPercentage(tracker.getCompletionPercentage())
                        .documentsVerified(tracker.getDocumentsVerified())
                        .portfolioVerified(tracker.getPortfolioVerified())
                        .serviceZoneSetup(tracker.getServiceZoneSetup())
                        .cinVerified(tracker.getCinVerified())
                        .emailVerified(tracker.getEmailVerified())
                        .phoneVerified(tracker.getPhoneVerified())
                        .profileCompleted(tracker.isVerificationComplete())
                        .build());
            }
            res = new ResponseEntity<>(barberDto, HttpStatus.OK);
        }
        return res;
    }


    @Operation(summary = "Get barber with associated lists by ID (Admin only)")
    @GetMapping("/{id}/with-lists")
    public ResponseEntity<BarberDto> findWithAssociatedLists(@PathVariable Long id) {
        return super.findWithAssociatedLists(id);
    }

    @Operation(summary = "Find paginated barbers by criteria (Admin only)")
    @PostMapping("/find-paginated-by-criteria")
    public ResponseEntity<PaginatedList> findPaginatedByCriteria(@RequestBody BarberCriteria criteria) throws Exception {
        return super.findPaginatedByCriteria(criteria);
    }

    @Operation(summary = "Find barbers by criteria (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<BarberDto>> findByCriteria(@RequestBody BarberCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Approve a barber registration (Admin only)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BarberDto> approveBarber(@PathVariable Long id) {
        try {
            logger.info("[AdminBarberController] Approving barber ID: {}", id);
            Barber approvedBarber = service.approveBarber(id);
            logger.info("[AdminBarberController] Barber approved successfully");
            return ResponseEntity.ok(converter.toDto(approvedBarber));
        } catch (IllegalArgumentException e) {
            logger.warn("[AdminBarberController] Barber not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            logger.error("[AdminBarberController] Error approving barber", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "Reject a barber registration (Admin only)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<BarberDto> rejectBarber(@PathVariable Long id) {
        try {
            logger.info("[AdminBarberController] Rejecting barber ID: {}", id);
            // TODO: Implement rejectBarber method in BarberService if needed
            Barber barber = service.findById(id);
            if (barber != null) {
                // You might want to set a status or delete the barber
                return ResponseEntity.ok(converter.toDto(barber));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("[AdminBarberController] Error rejecting barber", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "Update barber (Admin only)")
    @PutMapping
    public ResponseEntity<BarberDto> update(@RequestBody BarberDto dto) {
        try {
            logger.info("[AdminBarberController] Updating barber");
            Barber updatedBarber = service.update(converter.toItem(dto));
            logger.info("[AdminBarberController] Barber updated successfully");
            return ResponseEntity.ok(converter.toDto(updatedBarber));
        } catch (Exception e) {
            logger.error("[AdminBarberController] Error updating barber", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Delete barber by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    @Operation(summary = "Get barber statistics (Admin only)")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBarberStatistics() {
        try {
            // TODO: Implement statistics in AdminDashboardService
            Map<String, Object> stats = new HashMap<>();
            List<Barber> allBarbers = service.findAllOptimized();
            long totalBarbers = allBarbers.size();
            // Count barbers by status using repository
            long approvedBarbers = barberRepository.findByStatus(BarberStatus.APPROVED).size();
            long pendingBarbers = barberRepository.findByStatus(BarberStatus.PENDING_VALIDATION).size();
            long activeBarbers = barberRepository.findByStatus(BarberStatus.ACTIVE).size();

            stats.put("totalBarbers", totalBarbers);
            stats.put("approvedBarbers", approvedBarbers);
            stats.put("pendingBarbers", pendingBarbers);
            stats.put("activeBarbers", activeBarbers);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("[AdminBarberController] Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

