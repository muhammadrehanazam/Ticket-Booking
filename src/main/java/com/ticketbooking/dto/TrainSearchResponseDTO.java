package com.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainSearchResponseDTO {
    private Long scheduleId;
    private String trainNumber;
    private String trainName;
    private String fromStation;
    private String toStation;
    private LocalDate travelDate;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private List<ClassFareDTO> classFares;
}