package com.ticketbooking.repository;

import com.ticketbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPnrNumber(String pnrNumber);
    List<Booking> findByBookedById(Long userId);
    List<Booking> findByScheduleId(Long scheduleId);
}