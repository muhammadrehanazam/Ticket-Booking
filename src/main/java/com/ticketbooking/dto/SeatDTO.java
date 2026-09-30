package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SeatDTO {
    Long seatId;
    String seatNumber;
    String seatPosition;
    String coachNumber;
    String coachClass;
    String status;
}
