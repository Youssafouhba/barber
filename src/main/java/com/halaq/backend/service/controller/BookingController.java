package com.halaq.backend.service.controller;

import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.service.dto.AvailableSlotRequest;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.service.converter.BookingConverter;
import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.user.entity.Barber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Booking Management")
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController extends AbstractController<Booking, BookingDto, BookingCriteria, BookingService, BookingConverter> {

    public BookingController(BookingService service, BookingConverter converter) {
        super(service, converter);
    }


    @Operation(summary = "Finds bookings by criteria")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<BookingDto>> findByCriteria(@RequestBody BookingCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Creates a new booking")
    @PostMapping
    public ResponseEntity<BookingDto> save(@RequestBody BookingDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Updates an existing booking")
    @PutMapping
    public ResponseEntity<BookingDto> update(@RequestBody BookingDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Calculates available booking slots for barbers based on their availability and existing bookings")
    @PostMapping("/calculate-available-slots")
    public ResponseEntity<List<LocalDateTime>> calculateAvailableSlots(@RequestBody AvailableSlotRequest request) {
        return ResponseEntity.ok(service.calculateAvailableSlots(request));
    }

    @Operation(summary = "Finds a booking by its ID")
    @GetMapping("/id/{id}")
    public ResponseEntity<BookingDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }


    @Operation(summary = "Finds booking bybarber id")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<BookingDto>> findByBarberId(@PathVariable Long barberId) {
        List<Booking> bookings = service.findByBarberId(barberId);
        List<BookingDto> bookingDtos = converter.toDto(bookings);
        return ResponseEntity.ok(bookingDtos);
    }



    @Operation(summary = "Deletes a booking by its ID")
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    // --- CUSTOM ENDPOINTS FOR BUSINESS LOGIC ---

    @Operation(summary = "Finds all bookings for a specific client")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<BookingDto>> findByClientId(@PathVariable Long clientId) {
        List<Booking> bookings = service.findByClientId(clientId);
        return ResponseEntity.ok(converter.toCustomDto(bookings, Barber.class));
    }

    @Operation(summary = "Finds upcoming bookings for a specific barber")
    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingDto>> findUpcomingBookings() {
        List<Booking> bookings = service.findUpcomingBookings();
        return ResponseEntity.ok(converter.toDto(bookings));
    }

    @Operation(summary = "Endpoint for a barber to confirm a booking request")
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable Long id) {
       Booking confirmedBooking = service.confirmBooking(id);
        return ResponseEntity.ok(converter.toDto(confirmedBooking));
    }

    @Operation(summary = "Endpoint for a user (client or barber) to cancel a booking")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        Booking canceledBooking = service.cancelBooking(id);
        return ResponseEntity.ok(converter.toDto(canceledBooking));
    }

    @Operation(summary = "complete a booking")
    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingDto> completeBooking(@PathVariable Long id) {
        Booking completedBooking = service.completeBooking(id);
        return ResponseEntity.ok(converter.toDto(completedBooking));
    }
}