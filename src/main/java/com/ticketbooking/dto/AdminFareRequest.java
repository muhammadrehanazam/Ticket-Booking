package com.ticketbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminFareRequest {
    @NotBlank private String coachClass;
    @NotNull @Positive private Double fare;
}
