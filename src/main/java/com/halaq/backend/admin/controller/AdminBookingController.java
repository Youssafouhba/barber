package com.halaq.backend.admin.controller;

import com.halaq.backend.core.util.PaginatedList;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.service.converter.BookingConverter;
import com.halaq.backend.core.controller.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing all bookings in the system.
 * Only accessible by users with ADMIN role.
 */
@Tag(name = "Admin - Booking Management")
@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController extends AbstractController<Booking, BookingDto, BookingCriteria, BookingService, BookingConverter> {

    public AdminBookingController(BookingService service, BookingConverter converter) {
        super(service, converter);
    }

    @Operation(summary = "Get all bookings (Admin only)")
    @GetMapping
    public ResponseEntity<List<BookingDto>> findAll() throws Exception {
        return super.findAll();
    }

    @Operation(summary = "Get all bookings optimized (Admin only)")
    @GetMapping("/optimized")
    public ResponseEntity<List<BookingDto>> findAllOptimized() throws Exception {
        return super.findAllOptimized();
    }

    @Operation(summary = "Get booking by ID (Admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> findById(@PathVariable Long id) {
        return super.findById(id);
    }

    @Operation(summary = "Get booking with associated lists by ID (Admin only)")
    @GetMapping("/{id}/with-lists")
    public ResponseEntity<BookingDto> findWithAssociatedLists(@PathVariable Long id) {
        return super.findWithAssociatedLists(id);
    }

    @Operation(summary = "Get bookings by barber ID (Admin only)")
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<List<BookingDto>> findByBarberId(@PathVariable Long barberId) {
        List<Booking> bookings = service.findByBarberId(barberId);
        List<BookingDto> bookingDtos = converter.toDto(bookings);
        return ResponseEntity.ok(bookingDtos);
    }

    @Operation(summary = "Get bookings by client ID (Admin only)")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<BookingDto>> findByClientId(@PathVariable Long clientId) {
        List<Booking> bookings = service.findByClientId(clientId);
        return ResponseEntity.ok(converter.toDto(bookings));
    }

    @Operation(summary = "Get upcoming bookings (Admin only)")
    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingDto>> findUpcomingBookings() {
        List<Booking> bookings = service.findUpcomingBookings();
        return ResponseEntity.ok(converter.toDto(bookings));
    }

    @Operation(summary = "Find bookings by criteria (Admin only)")
    @PostMapping("/find-by-criteria")
    public ResponseEntity<List<BookingDto>> findByCriteria(@RequestBody BookingCriteria criteria) throws Exception {
        return super.findByCriteria(criteria);
    }

    @Operation(summary = "Find paginated bookings by criteria (Admin only)")
    @PostMapping("/find-paginated-by-criteria")
    public ResponseEntity<PaginatedList> findPaginatedByCriteria(@RequestBody BookingCriteria criteria) throws Exception {
        return super.findPaginatedByCriteria(criteria);
    }

    @Operation(summary = "Create a new booking (Admin only)")
    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestBody BookingDto dto) throws Exception {
        return super.save(dto);
    }

    @Operation(summary = "Update booking (Admin only)")
    @PutMapping
    public ResponseEntity<BookingDto> update(@RequestBody BookingDto dto) throws Exception {
        return super.update(dto);
    }

    @Operation(summary = "Delete booking by ID (Admin only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> deleteById(@PathVariable Long id) throws Exception {
        return super.deleteById(id);
    }

    @Operation(summary = "Cancel booking (Admin only)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        Booking canceledBooking = service.cancelBooking(id);
        return ResponseEntity.ok(converter.toDto(canceledBooking));
    }

    @Operation(summary = "Complete booking (Admin only)")
    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingDto> completeBooking(@PathVariable Long id) {
        Booking completedBooking = service.completeBooking(id);
        return ResponseEntity.ok(converter.toDto(completedBooking));
    }

    @Operation(summary = "Accept booking (Admin only)")
    @PostMapping("/{id}/accept")
    public ResponseEntity<BookingDto> acceptBooking(@PathVariable Long id) {
        Booking acceptedBooking = service.acceptBooking(id);
        return ResponseEntity.ok(converter.toDto(acceptedBooking));
    }

    @Operation(summary = "Reject booking (Admin only)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<BookingDto> rejectBooking(@PathVariable Long id) {
        Booking rejectedBooking = service.rejectBooking(id);
        return ResponseEntity.ok(converter.toDto(rejectedBooking));
    }
}

