package com.halaq.backend.user.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.user.converter.ServiceZoneConverter;
import com.halaq.backend.user.criteria.ServiceZoneCriteria;
import com.halaq.backend.user.dto.ServiceZoneDto;
import com.halaq.backend.user.entity.ServiceZone;
import com.halaq.backend.user.service.facade.ServiceZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Service Zones")
@RestController
@RequestMapping("/api/v1/service-zones")
public class ServiceZoneController extends AbstractController<ServiceZone, ServiceZoneDto, ServiceZoneCriteria, ServiceZoneService, ServiceZoneConverter> {

    public ServiceZoneController(ServiceZoneService service, ServiceZoneConverter converter) {
        super(service, converter);
    }



    @Operation(summary = "Find zones by barber id")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<ServiceZoneDto>> findByBarber(@PathVariable Long barberId) throws Exception {
        List<ServiceZone> zones = service.findByBarberId(barberId);
        return ResponseEntity.ok(converter.toDto(zones));
    }

    @Operation(summary = "Find or create zones by barber id")
    @PostMapping("/find-or-save")
    public ResponseEntity<List<ServiceZoneDto>> findOrCreateByBarber(@RequestBody List<ServiceZoneDto> zones) throws Exception {
        List<ServiceZone> createdZones = service.findOrCreate(converter.toItem(zones));
        return ResponseEntity.ok(converter.toDto(createdZones));
    }

    @Operation(summary = "Delete zone by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }
}


