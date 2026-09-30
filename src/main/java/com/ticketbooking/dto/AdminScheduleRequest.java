package com.ticketbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminScheduleRequest {
    @NotNull private Long trainId;
    @NotBlank private String sourceCity;
    @NotBlank private String destinationCity;
    @NotNull private LocalDate travelDate;
    @NotBlank private String departureTime;
    @NotBlank private String arrivalTime;
    private String duration;
}
