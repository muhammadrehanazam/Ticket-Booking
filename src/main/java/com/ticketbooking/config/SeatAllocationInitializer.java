package com.ticketbooking.config;

import com.ticketbooking.model.Schedule;
import com.ticketbooking.model.Seat;
import com.ticketbooking.model.SeatAllocation;
import com.ticketbooking.repository.ScheduleRepository;
import com.ticketbooking.repository.SeatAllocationRepository;
import com.ticketbooking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeatAllocationInitializer implements CommandLineRunner {

    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatAllocationRepository seatAllocationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (Schedule schedule : scheduleRepository.findAll()) {
            for (Seat seat : seatRepository.findByCoachTrainId(schedule.getTrain().getId())) {
                if (seatAllocationRepository.findByScheduleIdAndSeatId(schedule.getId(), seat.getId()).isEmpty()) {
                    seatAllocationRepository.save(SeatAllocation.builder()
                            .schedule(schedule)
                            .seat(seat)
                            .status("AVAILABLE")
                            .build());
                }
            }
        }
    }
}
