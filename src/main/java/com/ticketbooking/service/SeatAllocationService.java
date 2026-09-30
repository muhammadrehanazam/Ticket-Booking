package com.ticketbooking.service;

import com.ticketbooking.dto.SeatDTO;
import com.ticketbooking.dto.SeatLockRequest;
import com.ticketbooking.dto.SeatLockResponse;
import com.ticketbooking.model.Schedule;
import com.ticketbooking.model.Seat;
import com.ticketbooking.model.SeatAllocation;
import com.ticketbooking.repository.ScheduleRepository;
import com.ticketbooking.repository.SeatAllocationRepository;
import com.ticketbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatAllocationService {

    private static final int LOCK_MINUTES = 10;

    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatAllocationRepository seatAllocationRepository;

    @Transactional(readOnly = true)
    public List<SeatDTO> getSeats(Long scheduleId, String coachClass) {
        Schedule schedule = getSchedule(scheduleId);
        return seatRepository.findByCoachTrainId(schedule.getTrain().getId()).stream()
                .filter(seat -> coachClass == null || coachClass.equalsIgnoreCase(seat.getCoach().getCoachClass()))
                .sorted(Comparator.comparing(Seat::getId))
                .map(seat -> toSeatDTO(seat, getCurrentStatus(scheduleId, seat.getId())))
                .toList();
    }

    @Transactional
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        Schedule schedule = getSchedule(request.getScheduleId());
        releaseExpiredLocks();

        List<Long> seatIds = request.getSeatIds().stream().distinct().sorted().toList();
        if (seatIds.size() != request.getSeatIds().size()) {
            throw badRequest("Duplicate seat IDs are not allowed");
        }

        List<SeatAllocation> allocations = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> notFound("Seat not found: " + seatId));
            if (!seat.getCoach().getTrain().getId().equals(schedule.getTrain().getId())) {
                throw badRequest("Seat does not belong to this schedule's train: " + seatId);
            }

            SeatAllocation allocation = seatAllocationRepository
                    .findForUpdateByScheduleIdAndSeatId(schedule.getId(), seatId)
                    .orElseGet(() -> SeatAllocation.builder()
                            .schedule(schedule)
                            .seat(seat)
                            .status("AVAILABLE")
                            .build());

            if (!"AVAILABLE".equals(allocation.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Seat is not available: " + seat.getSeatNumber());
            }
            allocations.add(allocation);
        }

        String lockToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        allocations.forEach(allocation -> {
            allocation.setStatus("LOCKED");
            allocation.setLockToken(lockToken);
            allocation.setLockExpiryTime(expiry);
        });
        seatAllocationRepository.saveAll(allocations);

        return SeatLockResponse.builder()
                .lockToken(lockToken)
                .lockExpiryTime(expiry)
                .seats(allocations.stream()
                        .map(allocation -> toSeatDTO(allocation.getSeat(), "LOCKED"))
                        .toList())
                .build();
    }

    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public int releaseExpiredLocks() {
        return seatAllocationRepository.releaseExpiredLocks(LocalDateTime.now());
    }

    @Transactional
    public List<SeatAllocation> getLockedAllocations(Long scheduleId, String lockToken) {
        return seatAllocationRepository.findByScheduleIdAndLockToken(scheduleId, lockToken);
    }

    @Transactional
    public void saveAllocations(List<SeatAllocation> allocations) {
        seatAllocationRepository.saveAll(allocations);
    }

    @Transactional
    public void releaseBookedSeat(Long scheduleId, Long seatId) {
        SeatAllocation allocation = seatAllocationRepository
                .findForUpdateByScheduleIdAndSeatId(scheduleId, seatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Seat allocation not found"));
        allocation.setStatus("AVAILABLE");
        allocation.setLockToken(null);
        allocation.setLockExpiryTime(null);
        seatAllocationRepository.save(allocation);
    }

    private String getCurrentStatus(Long scheduleId, Long seatId) {
        return seatAllocationRepository.findByScheduleIdAndSeatId(scheduleId, seatId)
                .filter(allocation -> !"LOCKED".equals(allocation.getStatus())
                        || allocation.getLockExpiryTime() == null
                        || allocation.getLockExpiryTime().isAfter(LocalDateTime.now()))
                .map(SeatAllocation::getStatus)
                .orElse("AVAILABLE");
    }

    private Schedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Schedule not found: " + scheduleId));
    }

    private SeatDTO toSeatDTO(Seat seat, String status) {
        return SeatDTO.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatPosition(seat.getSeatPosition())
                .coachNumber(seat.getCoach().getCoachNumber())
                .coachClass(seat.getCoach().getCoachClass())
                .status(status)
                .build();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
