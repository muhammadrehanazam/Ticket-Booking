package com.ticketbooking.controller;

import com.ticketbooking.dto.SeatDTO;
import com.ticketbooking.dto.SeatLockRequest;
import com.ticketbooking.dto.SeatLockResponse;
import com.ticketbooking.service.SeatAllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SeatAllocationController {

    private final SeatAllocationService seatAllocationService;

    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<List<SeatDTO>> getSeats(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) String coachClass) {
        return ResponseEntity.ok(seatAllocationService.getSeats(scheduleId, coachClass));
    }

    @PostMapping("/seat-allocations/lock")
    public ResponseEntity<SeatLockResponse> lockSeats(@Valid @RequestBody SeatLockRequest request) {
        return ResponseEntity.ok(seatAllocationService.lockSeats(request));
    }
}
