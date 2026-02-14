package com.halaq.backend.service.controller;

import com.halaq.backend.core.controller.AbstractController;
import com.halaq.backend.core.security.entity.User;
import com.halaq.backend.service.converter.BookingConverter;
import com.halaq.backend.service.criteria.BookingCriteria;
import com.halaq.backend.service.dto.AvailableSlotRequest;
import com.halaq.backend.service.dto.BookingDto;
import com.halaq.backend.service.dto.TrackingInfoDTO;
import com.halaq.backend.service.entity.Booking;
import com.halaq.backend.service.service.facade.BookingService;
import com.halaq.backend.user.entity.Barber;
import com.halaq.backend.user.service.facade.BarberLiveLocationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.halaq.backend.core.security.common.SecurityUtil.getCurrentUser;

@Tag(name = "Booking Management")
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController extends AbstractController<Booking, BookingDto, BookingCriteria, BookingService, BookingConverter> {
    @Autowired
    private BarberLiveLocationsService locationService;
    public BookingController(BookingService service, BookingConverter converter) {
        super(service, converter);
    }


    @GetMapping("/{bookingId}/tracking")
    public ResponseEntity<TrackingInfoDTO> getBookingTracking(@PathVariable Long bookingId) {
        // 1. Récupérer le Booking
        Booking booking = service.findById(bookingId); // Votre service existant
        if (booking == null) return ResponseEntity.notFound().build();

        // 2. Sécurité : Vérifier que le user connecté est bien le client ou le barbier
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(booking.getClient().getId()) &&
                !currentUser.getId().equals(booking.getBarber().getId())) {
            return ResponseEntity.status(403).build();
        }

        // 3. Récupérer la position LIVE du barbier depuis Redis
        // On suppose que locationService a une méthode pour récupérer un point unique
        // Si elle n'existe pas, il faut l'ajouter dans BarberLiveLocationsService
        Point barberPos = locationService.getBarberLocation(booking.getBarber().getId());

        // Si pas de position Redis (barbier hors ligne/pas encore bougé), on prend sa position statique ou null
        double bLat = (barberPos != null) ? barberPos.getY() : 0.0; // Y = Lat
        double bLon = (barberPos != null) ? barberPos.getX() : 0.0; // X = Lon

        // 4. Construire le DTO
        TrackingInfoDTO dto = new TrackingInfoDTO();
        dto.setBookingId(booking.getId());
        dto.setStatus(booking.getStatus());

        dto.setBarberId(booking.getBarber().getId());
        dto.setBarberName(booking.getBarber().getFullName());
        dto.setBarberPhone(booking.getBarber().getPhone());
        dto.setBarberAvatar(booking.getBarber().getAvatar());

        dto.setClientLatitude(booking.getDestinationLatitude());
        dto.setClientLongitude(booking.getDestinationLongitude());

        dto.setBarberLatitude(bLat);
        dto.setBarberLongitude(bLon);

        // Calcul distance simple (Haversine) pour info
        // Vous pouvez aussi le faire côté front
        if (bLat != 0.0 && bLon != 0.0) {
            // ... calcul distance ...
            // dto.setDistanceKm(...);
        }

        return ResponseEntity.ok(dto);
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

    @Operation(summary = "Barber starts trip to client location")
    @PostMapping("/{id}/start-trip")
    public ResponseEntity<BookingDto> startTrip(@PathVariable Long id) {
        Booking booking = service.startTrip(id);
        return ResponseEntity.ok(converter.toDto(booking));
    }

    @Operation(summary = "Barber marks arrival at client location")
    @PostMapping("/{id}/mark-arrived")
    public ResponseEntity<BookingDto> markArrived(@PathVariable Long id) {
        Booking booking = service.markArrived(id);
        return ResponseEntity.ok(converter.toDto(booking));
    }

    @Operation(summary = "Barber starts the service")
    @PostMapping("/{id}/start-service")
    public ResponseEntity<BookingDto> startService(@PathVariable Long id) {
        Booking booking = service.startService(id);
        return ResponseEntity.ok(converter.toDto(booking));
    }
}