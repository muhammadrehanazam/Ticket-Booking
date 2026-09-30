package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class BookingResponse {
    Long bookingId;
    String pnrNumber;
    Long scheduleId;
    Double totalAmount;
    String paymentStatus;
    String bookingStatus;
    LocalDateTime bookingTime;
    LocalDateTime paymentDeadline;
    List<SeatDTO> seats;
}
