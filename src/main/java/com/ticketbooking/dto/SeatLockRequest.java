package com.ticketbooking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SeatLockRequest {
    @NotNull
    private Long scheduleId;

    @NotEmpty
    private List<Long> seatIds;
}
