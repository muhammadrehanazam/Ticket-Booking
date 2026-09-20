package com.ticketbooking.controller;

import com.ticketbooking.dto.TrainSearchResponseDTO;
import com.ticketbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/search")
    public ResponseEntity<List<TrainSearchResponseDTO>> searchTrains(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<TrainSearchResponseDTO> results = scheduleService.searchTrains(from, to, date);
        return ResponseEntity.ok(results);
    }
}