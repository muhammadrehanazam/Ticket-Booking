package com.ticketbooking.controller;

import com.ticketbooking.dto.*;
import com.ticketbooking.model.*;
import com.ticketbooking.security.UserPrincipal;
import com.ticketbooking.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/trains")
    public List<Map<String, Object>> trains(@AuthenticationPrincipal UserPrincipal principal) {
        adminService.requireAdmin(principal.getUserId());
        return adminService.trains().stream()
                .map(train -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("trainId", train.getId());
                    result.put("trainNumber", train.getTrainNumber());
                    result.put("name", train.getName());
                    return result;
                })
                .toList();
    }

    @PostMapping("/trains")
    public ResponseEntity<Map<String, Object>> createTrain(@AuthenticationPrincipal UserPrincipal principal,
                                                           @Valid @RequestBody AdminTrainRequest request) {
        adminService.requireAdmin(principal.getUserId());
        Train train = adminService.createTrain(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "trainId", train.getId(), "trainNumber", train.getTrainNumber(), "name", train.getName()));
    }

    @GetMapping("/schedules")
    public List<Map<String, Object>> schedules(@AuthenticationPrincipal UserPrincipal principal) {
        adminService.requireAdmin(principal.getUserId());
        return adminService.schedules().stream().map(schedule -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("scheduleId", schedule.getId());
            result.put("trainId", schedule.getTrain().getId());
            result.put("trainName", schedule.getTrain().getName());
            result.put("sourceCity", schedule.getSourceCity());
            result.put("destinationCity", schedule.getDestinationCity());
            result.put("travelDate", schedule.getTravelDate());
            result.put("departureTime", schedule.getDepartureTime());
            result.put("arrivalTime", schedule.getArrivalTime());
            result.put("duration", schedule.getDuration());
            return result;
        }).toList();
    }

    @PostMapping("/schedules")
    public ResponseEntity<Map<String, Object>> createSchedule(@AuthenticationPrincipal UserPrincipal principal,
                                                              @Valid @RequestBody AdminScheduleRequest request) {
        adminService.requireAdmin(principal.getUserId());
        Schedule schedule = adminService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "scheduleId", schedule.getId(), "trainId", schedule.getTrain().getId(),
                "sourceCity", schedule.getSourceCity(), "destinationCity", schedule.getDestinationCity(),
                "travelDate", schedule.getTravelDate()));
    }

    @PutMapping("/schedules/{scheduleId}/fares")
    public Map<String, Object> updateFare(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long scheduleId,
                                          @Valid @RequestBody AdminFareRequest request) {
        adminService.requireAdmin(principal.getUserId());
        ClassFare fare = adminService.upsertFare(scheduleId, request);
        return Map.of("fareId", fare.getId(), "scheduleId", scheduleId,
                "coachClass", fare.getCoachClass(), "fare", fare.getFare());
    }
}
