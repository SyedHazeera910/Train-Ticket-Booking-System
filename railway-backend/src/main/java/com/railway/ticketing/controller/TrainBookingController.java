package com.railway.ticketing.controller;

import com.railway.ticketing.dto.BookingRequest;
import com.railway.ticketing.dto.BookingResponse;
import com.railway.ticketing.entity.Station;
import com.railway.ticketing.entity.Train;
import com.railway.ticketing.repository.StationRepository;
import com.railway.ticketing.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrainBookingController {

    private final BookingService bookingService;
    private final StationRepository stationRepository;

    /* ─── Stations ─────────────────────────────────────────────────────── */

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getStations(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(
                stationRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(q, q));
        }
        return ResponseEntity.ok(stationRepository.findAll());
    }

    /* ─── Trains ────────────────────────────────────────────────────────── */

    @GetMapping("/trains/search")
    public ResponseEntity<List<Train>> searchTrains(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int seats) {
        return ResponseEntity.ok(bookingService.searchTrains(from, to, date, seats));
    }

    /* ─── Bookings ─────────────────────────────────────────────────────── */

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request, Authentication auth) {
        return ResponseEntity.ok(bookingService.createBooking(request, auth.getName()));
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication auth) {
        return ResponseEntity.ok(bookingService.getUserBookings(auth.getName()));
    }

    @GetMapping("/bookings/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getByPnr(@PathVariable String pnr) {
        return ResponseEntity.ok(bookingService.getByPnr(pnr));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, Authentication auth) {
        bookingService.cancelBooking(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
