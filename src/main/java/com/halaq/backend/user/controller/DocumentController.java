package com.halaq.backend.user.controller;

import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.core.util.FileStorageService;
import com.halaq.backend.shared.DocumentType;
import com.halaq.backend.user.converter.DocumentConverter;
import com.halaq.backend.user.criteria.DocumentCriteria;
import com.halaq.backend.user.dto.DocumentDto;
import com.halaq.backend.user.entity.Document;
import com.halaq.backend.user.service.facade.DocumentService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;
import static com.halaq.backend.core.util.FileStorageService.determineContentType;
import static com.halaq.backend.user.controller.BarberController.createErrorResponse;

@Tag(name = "Document Management")
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController extends AbstractController<Document, DocumentDto, DocumentCriteria, DocumentService, DocumentConverter> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final FileStorageService fileStorageService;
    public DocumentController(DocumentService service, DocumentConverter converter, FileStorageService fileStorageService) {
        super(service, converter);
        this.fileStorageService = fileStorageService;
    }


    /**
     * upload portfolio images for a barber
     */
    @PostMapping("/portfolio/upload")
    public ResponseEntity<List<DocumentDto>> uploadPortfolioImage(@RequestParam("files") MultipartFile[] files) {
        try {
            logger.info("[BarberController] Upload portfolio image");
            return ResponseEntity.ok().body(converter.toDto(service.uploadPortfolioImage(files)));
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors de l'upload portfolio image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @Operation(summary = "Download file by id")
    @GetMapping("/id/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            logger.info("[BarberController] Téléchargement avatar pour barbier: {}", id);
            byte[] fileContent = service.downloadFile(id);
            Document document = service.findById(id);
            String fileName = document.getUrl();

            String contentType = determineContentType(fileName);
            logger.info("[BarberController] Avatar téléchargé avec succès");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } catch (IllegalArgumentException e) {
            logger.warn("[BarberController] Avatar not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            logger.error("[BarberController] Erreur lors du téléchargement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    /**
     * Upload multiple files
     * @param files
     * @return
     * @throws Exception
     */
    @Operation(summary = "upload Diploma document")
    @PostMapping(value = "/upload-diploma", consumes = "multipart/form-data")
    public ResponseEntity<List<DocumentDto>> uploadDiplomaFile(@RequestParam("files") MultipartFile[] files) throws Exception {
        return ResponseEntity.ok(converter.toDto(service.uploadMultiple(files, DocumentType.DIPLOMA)));
    }

    @Operation(summary = "upload Cin document")
    @PostMapping(value = "/upload-cin", consumes = "multipart/form-data")
    public ResponseEntity<List<DocumentDto>> uploadCinFile(@RequestParam("files") MultipartFile[] files) throws Exception {
        return ResponseEntity.ok(converter.toDto(service.uploadMultiple(files, DocumentType.CIN)));
    }

    @Operation(summary = "upload Resum document")
    @PostMapping(value = "/upload-Resum", consumes = "multipart/form-data")
    public ResponseEntity<List<DocumentDto>> uploadResumeFile(@RequestParam("files") MultipartFile[] files) throws Exception {
        return ResponseEntity.ok(converter.toDto(service.uploadMultiple(files, DocumentType.RESUME)));
    }

    @Operation(summary = "upload Certification document")
    @PostMapping(value = "/upload-certification", consumes = "multipart/form-data")
    public ResponseEntity<List<DocumentDto>> uploadCertificationFile(@RequestParam("files") MultipartFile[] files) throws Exception {
        return ResponseEntity.ok(converter.toDto(service.uploadMultiple(files, DocumentType.CERTIFICATION)));
    }

    @Operation(summary = "Finds documents by criteria")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<DocumentDto>> findByCriteria(@RequestBody DocumentCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Finds a document by id")
    @GetMapping("/id/{id}")
    public ResponseEntity<DocumentDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    @Operation(summary = "Saves the specified document")
    @PostMapping("/")
    public ResponseEntity<DocumentDto> save(@RequestBody DocumentDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Deletes the specified document")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }
}
