package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class SeatLockResponse {
    String lockToken;
    LocalDateTime lockExpiryTime;
    List<SeatDTO> seats;
}
