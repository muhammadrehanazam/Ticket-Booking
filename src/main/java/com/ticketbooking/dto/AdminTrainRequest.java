package com.ticketbooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminTrainRequest {
    @NotBlank
    private String trainNumber;
    @NotBlank
    private String name;
}
