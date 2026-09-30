package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class BookingDetailsResponse {
    Long bookingId;
    String pnrNumber;
    Long scheduleId;
    String trainNumber;
    String trainName;
    String fromStation;
    String toStation;
    Double totalAmount;
    String paymentStatus;
    String bookingStatus;
    LocalDateTime bookingTime;
    LocalDateTime paymentDeadline;
    List<PassengerDTO> passengers;
}
