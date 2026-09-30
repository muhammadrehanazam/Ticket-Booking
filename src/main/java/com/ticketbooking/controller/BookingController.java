package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/pnr/{pnrNumber}")
    public ResponseEntity<BookingDetailsResponse> getByPnr(@PathVariable String pnrNumber) {
        return ResponseEntity.ok(bookingService.getByPnr(pnrNumber));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDetailsResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getByUser(userId));
    }

    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<BookingDetailsResponse> payBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.payBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDetailsResponse> cancelBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}
