package com.halaq.backend.service.controller;

import com.halaq.backend.service.converter.AvailabilityConverter;
import com.halaq.backend.service.criteria.AvailabilityCriteria;
import com.halaq.backend.service.dto.AvailabilityDto;
import com.halaq.backend.service.entity.Availability;
import com.halaq.backend.service.service.facade.AvailabilityService;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Barber Availability")
@RestController
@RequestMapping("/api/v1/availabilities")
public class AvailabilityController extends AbstractController<Availability, AvailabilityDto, AvailabilityCriteria, AvailabilityService, AvailabilityConverter> {

    public AvailabilityController(AvailabilityService service, AvailabilityConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Get all availabilities for a specific barber")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<AvailabilityDto>> findByBarberId(@PathVariable Long barberId) {
        List<Availability> availabilities = service.findByBarberId(barberId);
        return ResponseEntity.ok(converter.toDto(availabilities));
    }

    @Operation(summary = "Create a new availability slot for a barber")
    @PostMapping("/")
    public ResponseEntity<AvailabilityDto> save(@RequestBody AvailabilityDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Create multiple availabilities for a barber")
    @PostMapping("/multiple")
    public ResponseEntity<List<AvailabilityDto>> saveMultiple(@RequestBody List<AvailabilityDto> dtos) throws Exception {
        List<AvailabilityDto> response = new ArrayList<>();
        for (AvailabilityDto dto : dtos) {
            response.add(converter.toDto(service.create(converter.toItem(dto))));
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace all availabilities for a barber (Overwrite)")
    @PutMapping("/replace") // PUT est plus logique pour un remplacement total
    public ResponseEntity<List<AvailabilityDto>> replaceMultiple(@RequestBody List<AvailabilityDto> dtos) throws Exception {
        if (dtos == null || dtos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // 1. Convertir toute la liste d'un coup
        List<Availability> items = dtos.stream()
                .map(converter::toItem)
                .collect(Collectors.toList());

        // 2. Appeler le service pour "écraser et sauvegarder"
        List<Availability> savedItems = service.replaceAvailabilities(items);

        // 3. Convertir le résultat en DTO
        List<AvailabilityDto> resultDtos = savedItems.stream()
                .map(converter::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultDtos);
    }

    @Operation(summary = "Update an availability slot")
    @PutMapping("/")
    public ResponseEntity<AvailabilityDto> update(@RequestBody AvailabilityDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Delete an availability slot by id")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }
}
