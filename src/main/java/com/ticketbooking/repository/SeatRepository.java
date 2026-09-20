package com.ticketbooking.repository;

import com.ticketbooking.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCoachId(Long coachId);
    List<Seat> findByCoachTrainId(Long trainId);
}