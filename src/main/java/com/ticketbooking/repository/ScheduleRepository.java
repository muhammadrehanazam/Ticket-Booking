package com.ticketbooking.repository;

import com.ticketbooking.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findBySourceCityIgnoreCaseAndDestinationCityIgnoreCaseAndTravelDate(
            String sourceCity,
            String destinationCity,
            LocalDate travelDate
    );
}