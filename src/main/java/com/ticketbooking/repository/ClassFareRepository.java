package com.ticketbooking.repository;

import com.ticketbooking.model.ClassFare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassFareRepository extends JpaRepository<ClassFare, Long> {
    List<ClassFare> findByScheduleId(Long scheduleId);
    Optional<ClassFare> findByScheduleIdAndCoachClass(Long scheduleId, String coachClass);
}