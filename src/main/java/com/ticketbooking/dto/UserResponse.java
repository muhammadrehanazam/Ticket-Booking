package com.ticketbooking.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {
    Long userId;
    String name;
    String phone;
    String cnic;
    String email;
    String role;
    String token;
}
