package com.ticketbooking.repository;

import com.ticketbooking.model.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {
    List<SeatAllocation> findByScheduleId(Long scheduleId);
    Optional<SeatAllocation> findByScheduleIdAndSeatId(Long scheduleId, Long seatId);

    // Expired locks ko check aur release karne ke liye
    List<SeatAllocation> findByStatusAndLockExpiryTimeBefore(String status, LocalDateTime time);
}