package com.halaq.backend.service.controller;

import com.halaq.backend.service.converter.ServiceCategoryConverter;
import com.halaq.backend.service.criteria.ServiceCategoryCriteria;
import com.halaq.backend.service.dto.ServiceCategoryDto;
import com.halaq.backend.service.entity.ServiceCategory;
import com.halaq.backend.service.service.facade.ServiceCategoryService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Service Categories")
@RestController
@RequestMapping("/api/v1/service-categories")
public class ServiceCategoryController extends AbstractController<ServiceCategory, ServiceCategoryDto, ServiceCategoryCriteria, ServiceCategoryService, ServiceCategoryConverter> {

    public ServiceCategoryController(ServiceCategoryService service, ServiceCategoryConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Finds all service categories")
    @GetMapping
    public ResponseEntity<List<ServiceCategoryDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Finds a service category by id")
    @GetMapping("/id/{id}")
    public ResponseEntity<ServiceCategoryDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    // Note: POST, PUT, DELETE operations for service categories have been moved to AdminServiceCategoryController
    // Only admins can create, update, or delete service categories via /api/admin/service-categories
    // Regular users can only view categories via this controller
}