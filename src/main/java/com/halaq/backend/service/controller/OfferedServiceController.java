package com.halaq.backend.service.controller;

import com.halaq.backend.core.util.PaginatedList;
import com.halaq.backend.service.converter.OfferedServiceConverter;
import com.halaq.backend.service.criteria.OfferedServiceCriteria;
import com.halaq.backend.service.dto.OfferedServiceDto;
import com.halaq.backend.service.entity.OfferedService;
import com.halaq.backend.service.service.facade.OfferedServiceService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Services")
@RestController
@RequestMapping("/api/v1/offered-services")
public class OfferedServiceController extends AbstractController<OfferedService, OfferedServiceDto, OfferedServiceCriteria, OfferedServiceService, OfferedServiceConverter> {


    public OfferedServiceController(OfferedServiceService service, OfferedServiceConverter converter) {
        super(service, converter);
    }

    /**
     * Endpoint POST pour qu'un coiffeur crée un nouveau service offert.
     * @param request Le DTO contenant les détails du service et l'ID de la catégorie.
     * @return ResponseEntity contenant le service créé et le statut HTTP 201 (Created).
     */
    @Operation(summary = "create a service")
    @PostMapping
    public ResponseEntity<OfferedServiceDto> create(@Valid @RequestBody OfferedServiceDto request) throws Exception {
        return super.save(request);
    }

    @Operation(summary = "Finds all services")
    @GetMapping
    public ResponseEntity<List<OfferedServiceDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Finds all services for a given category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<OfferedServiceDto>> findByCategoryId(@PathVariable Long categoryId) {
        List<OfferedService> services = service.findByCategoryId(categoryId);
        return ResponseEntity.ok(converter.toDto(services));
    }

    @Operation(summary = "Finds a service by id")
    @GetMapping("/id/{id}")
    public ResponseEntity<OfferedServiceDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }




    @Operation(summary = "Updates the specified  service")
    @PutMapping
    public ResponseEntity<OfferedServiceDto> update(@RequestBody OfferedServiceDto dto) throws Exception {
        return super.update(dto);
    }


    @Operation(summary = "Delete the specified service")
    @DeleteMapping("id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }


    @Operation(summary = "find paged services")
    @PostMapping("/find-paged")
    public ResponseEntity<List<OfferedServiceDto>> findByCriteria(@RequestBody OfferedServiceCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "find bypaginatedcriteria")
    @PostMapping("/find-by-paginated-criteria")
    public ResponseEntity<PaginatedList> findByPaginatedCriteria(@RequestBody OfferedServiceCriteria criteria) throws Exception {
        return super.findPaginatedByCriteria(criteria);
    }
}