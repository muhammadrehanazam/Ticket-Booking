package com.ticketbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long scheduleId;

    @NotBlank
    private String lockToken;

    @NotEmpty
    @Valid
    private List<PassengerRequest> passengers;
}
