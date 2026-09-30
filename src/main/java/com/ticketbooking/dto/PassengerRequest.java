package com.ticketbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PassengerRequest {
    @NotBlank
    private String passengerName;

    @NotBlank
    private String cnicOrBForm;

    @Min(1)
    private Integer age;

    @NotBlank
    private String gender;
}
