package com.halaq.backend.service.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.service.converter.TimeBlockConverter;
import com.halaq.backend.service.criteria.TimeBlockCriteria;
import com.halaq.backend.service.dto.TimeBlockDto;
import com.halaq.backend.service.entity.TimeBlock;
import com.halaq.backend.service.service.facade.TimeBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Time Blocks")
@RestController
@RequestMapping("/api/v1/time-blocks")
public class TimeBlockController extends AbstractController<TimeBlock, TimeBlockDto, TimeBlockCriteria, TimeBlockService, TimeBlockConverter> {

    public TimeBlockController(TimeBlockService service, TimeBlockConverter converter) {
        super(service, converter);
    }


    @Operation(summary = "save a time block")
    @PostMapping
    public ResponseEntity<TimeBlockDto> save(@RequestBody TimeBlockDto dto) throws Exception {
        return super.save(dto);
    }


    @Operation(summary = "Update  a Time Block")
    @PutMapping
    public ResponseEntity<TimeBlockDto> update(@RequestBody TimeBlockDto dto) throws Exception {
        return super.update(dto);
    }


    @Operation(summary = "Finds time blocks by barber ID")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<TimeBlockDto>> findByBarber(@PathVariable Long barberId) {
        List<TimeBlock> list = service.findByBarberId(barberId);
        List<TimeBlockDto> dto = converter.toDto(list);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Finds time blocks by barber ID and date range")
    @GetMapping("/barber/{barberId}/between")
    public ResponseEntity<List<TimeBlockDto>> findByBarberAndBetween(
            @PathVariable Long barberId,
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end
    ) {
        List<TimeBlock> list = service.findByBarberIdAndBetween(barberId, start, end);
        return ResponseEntity.ok(converter.toDto(list));
    }

    @Operation(summary = "Deletes a booking by its ID")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }
}


