package com.ticketbooking.config;

import com.ticketbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingPaymentExpiryScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedDelay = 30000)
    public void expirePendingPayments() {
        bookingService.expirePendingBookings();
    }
}
