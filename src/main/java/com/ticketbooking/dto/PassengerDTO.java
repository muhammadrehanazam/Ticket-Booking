package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PassengerDTO {
    Long passengerId;
    String passengerName;
    String cnicOrBForm;
    Integer age;
    String gender;
    SeatDTO seat;
}
