package com.ticketbooking;

import com.ticketbooking.dto.*;
import com.ticketbooking.model.Schedule;
import com.ticketbooking.model.SeatAllocation;
import com.ticketbooking.repository.ScheduleRepository;
import com.ticketbooking.repository.SeatAllocationRepository;
import com.ticketbooking.service.BookingService;
import com.ticketbooking.service.SeatAllocationService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingFlowIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private SeatAllocationService seatAllocationService;
    @Autowired private BookingService bookingService;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private SeatAllocationRepository seatAllocationRepository;

    @Test
    void completesBookingPaymentCancellationAndReleasesSeat() {
        RegisterRequest register = new RegisterRequest();
        register.setName("Integration User");
        register.setPhone("0300" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        register.setCnic("42101-" + UUID.randomUUID().toString().replace("-", "").substring(0, 7) + "-1");
        register.setEmail("integration@example.com");
        register.setPassword("Test1234");

        UserResponse user = userService.register(register);
        assertEquals(user.getUserId(), userService.login(login(register)).getUserId());

        Schedule schedule = scheduleRepository
                .findBySourceCityIgnoreCaseAndDestinationCityIgnoreCaseAndTravelDate(
                        "Karachi Cantt", "Lahore Jn", LocalDate.now().plusDays(1))
                .get(0);
        List<SeatDTO> seats = seatAllocationService.getSeats(schedule.getId(), "AC_BUSINESS");
        SeatDTO availableSeat = seats.stream()
                .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                .findFirst()
                .orElseThrow();

        SeatLockRequest lockRequest = new SeatLockRequest();
        lockRequest.setScheduleId(schedule.getId());
        lockRequest.setSeatIds(List.of(availableSeat.getSeatId()));
        SeatLockResponse lock = seatAllocationService.lockSeats(lockRequest);

        assertThrows(ResponseStatusException.class,
                () -> seatAllocationService.lockSeats(lockRequest));

        PassengerRequest passenger = new PassengerRequest();
        passenger.setPassengerName("Integration Passenger");
        passenger.setCnicOrBForm("42101-7654321-2");
        passenger.setAge(28);
        passenger.setGender("MALE");

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(user.getUserId());
        bookingRequest.setScheduleId(schedule.getId());
        bookingRequest.setLockToken(lock.getLockToken());
        bookingRequest.setPassengers(List.of(passenger));

        BookingResponse booking = bookingService.createBooking(bookingRequest);
        assertEquals("CONFIRMED", booking.getBookingStatus());
        assertEquals("PENDING", booking.getPaymentStatus());

        BookingDetailsResponse paid = bookingService.payBooking(booking.getBookingId());
        assertEquals("PAID", paid.getPaymentStatus());
        assertThrows(ResponseStatusException.class,
                () -> bookingService.payBooking(booking.getBookingId()));

        BookingDetailsResponse cancelled = bookingService.cancelBooking(booking.getBookingId());
        assertEquals("CANCELLED", cancelled.getBookingStatus());
        assertEquals("PAID", cancelled.getPaymentStatus());
        assertThrows(ResponseStatusException.class,
                () -> bookingService.cancelBooking(booking.getBookingId()));
        assertEquals("CANCELLED", bookingService.getByPnr(booking.getPnrNumber()).getBookingStatus());

        SeatAllocation allocation = seatAllocationRepository
                .findByScheduleIdAndSeatId(schedule.getId(), availableSeat.getSeatId())
                .orElseThrow();
        assertEquals("AVAILABLE", allocation.getStatus());
    }

    private LoginRequest login(RegisterRequest register) {
        LoginRequest request = new LoginRequest();
        request.setPhone(register.getPhone());
        request.setPassword(register.getPassword());
        return request;
    }
}
