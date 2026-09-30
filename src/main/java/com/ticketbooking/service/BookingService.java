package com.ticketbooking.service;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.PassengerRequest;
import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.dto.PassengerDTO;
import com.ticketbooking.dto.SeatDTO;
import com.ticketbooking.model.Booking;
import com.ticketbooking.model.Passenger;
import com.ticketbooking.model.Schedule;
import com.ticketbooking.model.SeatAllocation;
import com.ticketbooking.model.User;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.ClassFareRepository;
import com.ticketbooking.repository.ScheduleRepository;
import com.ticketbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int PAYMENT_WINDOW_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassFareRepository classFareRepository;
    private final SeatAllocationService seatAllocationService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> notFound("User not found: " + request.getUserId()));
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> notFound("Schedule not found: " + request.getScheduleId()));

        List<SeatAllocation> allocations = seatAllocationService
                .getLockedAllocations(schedule.getId(), request.getLockToken());
        if (allocations.size() != request.getPassengers().size()
                || allocations.isEmpty()
                || allocations.stream().anyMatch(this::isExpired)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seat lock is invalid, expired, or passenger count does not match locked seats");
        }

        String coachClass = allocations.get(0).getSeat().getCoach().getCoachClass();
        if (allocations.stream().anyMatch(allocation ->
                !coachClass.equals(allocation.getSeat().getCoach().getCoachClass()))) {
            throw badRequest("All seats in one booking must belong to the same coach class");
        }

        Double fare = classFareRepository.findByScheduleIdAndCoachClass(schedule.getId(), coachClass)
                .orElseThrow(() -> notFound("Fare not found for coach class: " + coachClass))
                .getFare();

        LocalDateTime bookingTime = LocalDateTime.now();
        Booking booking = Booking.builder()
                .pnrNumber("PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bookedBy(user)
                .schedule(schedule)
                .totalAmount(fare * request.getPassengers().size())
                .paymentStatus("PENDING")
                .bookingStatus("CONFIRMED")
                .bookingTime(bookingTime)
                .paymentDeadline(bookingTime.plusMinutes(PAYMENT_WINDOW_MINUTES))
                .build();

        List<Passenger> passengers = request.getPassengers().stream()
                .map(passenger -> toPassenger(passenger, booking, allocations.remove(0).getSeat()))
                .toList();
        booking.setPassengers(passengers);

        List<SeatAllocation> lockedAllocations = seatAllocationService
                .getLockedAllocations(schedule.getId(), request.getLockToken());
        for (SeatAllocation allocation : lockedAllocations) {
            allocation.setStatus("BOOKED");
            allocation.setLockToken(null);
            allocation.setLockExpiryTime(null);
        }
        seatAllocationService.saveAllocations(lockedAllocations);

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved, passengers);
    }

    @Transactional(readOnly = true)
    public BookingDetailsResponse getByPnr(String pnrNumber) {
        Booking booking = bookingRepository.findByPnrNumber(pnrNumber)
                .orElseThrow(() -> notFound("Booking not found for PNR: " + pnrNumber));
        return toDetailsResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw notFound("User not found: " + userId);
        }
        return bookingRepository.findByBookedById(userId).stream()
                .map(this::toDetailsResponse)
                .toList();
    }

    @Transactional
    public BookingDetailsResponse payBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> notFound("Booking not found: " + bookingId));
        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only confirmed bookings can be paid");
        }
        if ("PENDING".equals(booking.getPaymentStatus())
                && booking.getPaymentDeadline() != null
                && !booking.getPaymentDeadline().isAfter(LocalDateTime.now())) {
            expireBooking(booking);
            bookingRepository.save(booking);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment window expired; booking was cancelled");
        }
        if ("PAID".equals(booking.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking is already paid");
        }
        booking.setPaymentStatus("PAID");
        return toDetailsResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDetailsResponse cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> notFound("Booking not found: " + bookingId));
        if (!"CONFIRMED".equals(booking.getBookingStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only confirmed bookings can be cancelled");
        }

        booking.getPassengers().forEach(passenger ->
                seatAllocationService.releaseBookedSeat(
                        booking.getSchedule().getId(), passenger.getSeat().getId()));
        booking.setBookingStatus("CANCELLED");
        return toDetailsResponse(bookingRepository.save(booking));
    }

    @Transactional
    public int expirePendingBookings() {
        int expired = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookingRepository.findAll()) {
            if ("CONFIRMED".equals(booking.getBookingStatus())
                    && "PENDING".equals(booking.getPaymentStatus())
                    && booking.getPaymentDeadline() != null
                    && !booking.getPaymentDeadline().isAfter(now)) {
                expireBooking(booking);
                bookingRepository.save(booking);
                expired++;
            }
        }
        return expired;
    }

    private void expireBooking(Booking booking) {
        booking.getPassengers().forEach(passenger ->
                seatAllocationService.releaseBookedSeat(
                        booking.getSchedule().getId(), passenger.getSeat().getId()));
        booking.setBookingStatus("CANCELLED");
        booking.setPaymentStatus("EXPIRED");
    }

    private Passenger toPassenger(PassengerRequest request, Booking booking, com.ticketbooking.model.Seat seat) {
        return Passenger.builder()
                .passengerName(request.getPassengerName())
                .cnicOrBForm(request.getCnicOrBForm())
                .age(request.getAge())
                .gender(request.getGender())
                .seat(seat)
                .booking(booking)
                .build();
    }

    private BookingResponse toResponse(Booking booking, List<Passenger> passengers) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .pnrNumber(booking.getPnrNumber())
                .scheduleId(booking.getSchedule().getId())
                .totalAmount(booking.getTotalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .bookingStatus(booking.getBookingStatus())
                .bookingTime(booking.getBookingTime())
                .paymentDeadline(booking.getPaymentDeadline())
                .seats(passengers.stream().map(passenger -> SeatDTO.builder()
                        .seatId(passenger.getSeat().getId())
                        .seatNumber(passenger.getSeat().getSeatNumber())
                        .seatPosition(passenger.getSeat().getSeatPosition())
                        .coachNumber(passenger.getSeat().getCoach().getCoachNumber())
                        .coachClass(passenger.getSeat().getCoach().getCoachClass())
                        .status("BOOKED")
                        .build()).toList())
                .build();
    }

    private BookingDetailsResponse toDetailsResponse(Booking booking) {
        return BookingDetailsResponse.builder()
                .bookingId(booking.getId())
                .pnrNumber(booking.getPnrNumber())
                .scheduleId(booking.getSchedule().getId())
                .trainNumber(booking.getSchedule().getTrain().getTrainNumber())
                .trainName(booking.getSchedule().getTrain().getName())
                .fromStation(booking.getSchedule().getSourceCity())
                .toStation(booking.getSchedule().getDestinationCity())
                .totalAmount(booking.getTotalAmount())
                .paymentStatus(booking.getPaymentStatus())
                .bookingStatus(booking.getBookingStatus())
                .bookingTime(booking.getBookingTime())
                .paymentDeadline(booking.getPaymentDeadline())
                .passengers(booking.getPassengers().stream().map(passenger ->
                        PassengerDTO.builder()
                                .passengerId(passenger.getId())
                                .passengerName(passenger.getPassengerName())
                                .cnicOrBForm(passenger.getCnicOrBForm())
                                .age(passenger.getAge())
                                .gender(passenger.getGender())
                                .seat(SeatDTO.builder()
                                        .seatId(passenger.getSeat().getId())
                                        .seatNumber(passenger.getSeat().getSeatNumber())
                                        .seatPosition(passenger.getSeat().getSeatPosition())
                                        .coachNumber(passenger.getSeat().getCoach().getCoachNumber())
                                        .coachClass(passenger.getSeat().getCoach().getCoachClass())
                                        .status("CANCELLED".equals(booking.getBookingStatus())
                                                ? "AVAILABLE" : "BOOKED")
                                        .build())
                                .build()).toList())
                .build();
    }

    private boolean isExpired(SeatAllocation allocation) {
        return allocation.getLockExpiryTime() == null
                || !allocation.getLockExpiryTime().isAfter(LocalDateTime.now());
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
