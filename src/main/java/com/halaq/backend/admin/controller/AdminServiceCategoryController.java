package com.halaq.backend.admin.controller;

import com.halaq.backend.core.util.PaginatedList;
import com.halaq.backend.service.converter.ServiceCategoryConverter;
import com.halaq.backend.service.criteria.ServiceCategoryCriteria;
import com.halaq.backend.service.dto.ServiceCategoryDto;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing service categories.
 * Only accessible by users with ADMIN role.
 * Regular users can only view categories, but admin can create, update, and delete them.
 */
@Tag(name = "Admin - Service Category Management")
@RestController
@RequestMapping("/api/admin/service-categories")
public class AdminServiceCategoryController extends AbstractController<ServiceCategory, ServiceCategoryDto, ServiceCategoryCriteria, ServiceCategoryService, ServiceCategoryConverter> {

    public AdminServiceCategoryController(ServiceCategoryService service, ServiceCategoryConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Get all service categories (Admin only)")
    @GetMapping
    public ResponseEntity<List<ServiceCategoryDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Get all service categories optimized (Admin only)")
    @GetMapping("/optimized")
    public ResponseEntity<List<ServiceCategoryDto>> findAllOptimized() throws Exception {
        return super.findAllOptimized();
    }

    @Operation(summary = "Get service category by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    @Operation(summary = "Get service category with associated lists by ID (Admin only)")
    @GetMapping("/{id}/with-lists")
    public ResponseEntity<ServiceCategoryDto> findWithAssociatedLists(@PathVariable Long id) {
        return super.findWithAssociatedLists(id);
    }

    @Operation(summary = "Create a new service category (Admin only)")
    @PostMapping
    public ResponseEntity<ServiceCategoryDto> create(@Valid @RequestBody ServiceCategoryDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Update service category (Admin only)")
    @PutMapping
    public ResponseEntity<ServiceCategoryDto> update(@Valid @RequestBody ServiceCategoryDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Delete service category by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    @Operation(summary = "Find service categories by criteria (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<ServiceCategoryDto>> findByCriteria(@RequestBody ServiceCategoryCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Find paginated service categories by criteria (Admin only)")
    @PostMapping("/find-paginated-by-criteria")
    public ResponseEntity<PaginatedList> findPaginatedByCriteria(@RequestBody ServiceCategoryCriteria criteria) throws Exception {
        return super.findPaginatedByCriteria(criteria);
    }
}

