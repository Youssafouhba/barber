package com.halaq.backend.user.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.core.transverse.cloud.dto.FileUploadResult;
import com.halaq.backend.user.converter.ClientConverter;
import com.halaq.backend.user.criteria.ClientCriteria;
import com.halaq.backend.user.dto.ClientDto;
import com.halaq.backend.user.entity.Client;
import com.halaq.backend.user.service.facade.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.halaq.backend.user.controller.BarberController.createErrorResponse;

@Tag(name = "Client Management")
@RestController
@RequestMapping("/api/v1/clients/")
public class ClientController extends AbstractController<Client, ClientDto, ClientCriteria, ClientService, ClientConverter> {
    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClientController.class);
    public ClientController(ClientService service, ClientConverter converter) {
        super(service, converter);
    }

    /**
     * Upload avatar pour un barbier
     * Supprime l'ancien avatar automatiquement
     */
    @PostMapping("/{clientId}/avatar/upload")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long clientId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            logger.warn("[ClientController] Avatar upload - File is empty");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("File cannot be empty", "400"));
        }

        try {
            logger.info("[ClientController] Avatar upload pour barbier: {}", clientId);
            FileUploadResult result = service.uploadClientAvatar(clientId, file);
            logger.info("[ClientController] Avatar uploadé avec succès");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("[ClientController] Client not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Client not found", "404"));
        } catch (Exception e) {
            logger.error("[ClientController] Erreur lors de l'upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    /**
     * Télécharger l'avatar d'un barbier
     */
    @GetMapping("/{clientId}/avatar/download")
    public ResponseEntity<?> downloadAvatar(@PathVariable Long clientId) {
        try {
            logger.info("[ClientController] Téléchargement avatar pour barbier: {}", clientId);
            byte[] fileContent = service.downloadClientAvatar(clientId);

            logger.info("[ClientController] Avatar téléchargé avec succès");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"avatar.jpg\"")
                    .body(fileContent);
        } catch (IllegalArgumentException e) {
            logger.warn("[ClientController] Avatar not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Avatar not found", "404"));
        } catch (Exception e) {
            logger.error("[ClientController] Erreur lors du téléchargement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage(), "500"));
        }
    }

    @Operation(summary = "Finds all clients")
    @GetMapping
    public ResponseEntity<List<ClientDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Finds all clients optimized")
    @GetMapping("optimized")
    public ResponseEntity<List<ClientDto>> findAllOptimized() throws Exception {
        return super.findAllOptimized();
    }

    @Operation(summary = "Finds a client by ID")
    @GetMapping("id/{id}")
    public ResponseEntity<ClientDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    @Operation(summary = "Finds clients by criteria")
    @PostMapping("find-by-criteria")
    public ResponseEntity<List<ClientDto>> findByCriteria(@RequestBody ClientCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Updates an existing client")
    @PutMapping
    public ResponseEntity<ClientDto> update(@RequestBody ClientDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Deletes a client by its ID")
    @DeleteMapping("id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

}
