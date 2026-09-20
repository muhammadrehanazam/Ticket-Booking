package com.ticketbooking.config;

import com.ticketbooking.model.*;
import com.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TrainRepository trainRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassFareRepository classFareRepository;

    @Override
    public void run(String... args) {
        if (trainRepository.count() > 0) {
            return; // Data already loaded
        }

        // 1. Train create karein
        Train train = Train.builder()
                .trainNumber("11UP")
                .name("Karakoram Express")
                .build();
        trainRepository.save(train);

        // 2. Coaches create karein
        Coach coachEconomy = Coach.builder()
                .coachNumber("Coach-1")
                .coachClass("ECONOMY")
                .train(train)
                .build();

        Coach coachAcBusiness = Coach.builder()
                .coachNumber("Coach-2")
                .coachClass("AC_BUSINESS")
                .train(train)
                .build();

        coachRepository.save(coachEconomy);
        coachRepository.save(coachAcBusiness);

        // 3. Seats create karein (Economy: 10 seats, AC Business: 6 seats)
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            seats.add(Seat.builder()
                    .seatNumber(String.valueOf(i))
                    .seatPosition(i % 2 == 0 ? "WINDOW_SEAT" : "AISLE_SEAT")
                    .coach(coachEconomy)
                    .build());
        }

        for (int i = 1; i <= 6; i++) {
            seats.add(Seat.builder()
                    .seatNumber(String.valueOf(i))
                    .seatPosition(i % 2 == 0 ? "UPPER_BERTH" : "LOWER_BERTH")
                    .coach(coachAcBusiness)
                    .build());
        }
        seatRepository.saveAll(seats);

        // 4. Schedule create karein
        Schedule schedule = Schedule.builder()
                .train(train)
                .sourceCity("Karachi Cantt")
                .destinationCity("Lahore Jn")
                .travelDate(LocalDate.now().plusDays(1)) // Kal ki date
                .departureTime("15:00")
                .arrivalTime("10:20 (+1)")
                .duration("19 h 20 min")
                .build();
        scheduleRepository.save(schedule);

        // 5. Class Fares assign karein
        ClassFare fareEconomy = ClassFare.builder()
                .schedule(schedule)
                .coachClass("ECONOMY")
                .fare(3750.0)
                .build();

        ClassFare fareAc = ClassFare.builder()
                .schedule(schedule)
                .coachClass("AC_BUSINESS")
                .fare(10950.0)
                .build();

        classFareRepository.save(fareEconomy);
        classFareRepository.save(fareAc);

        System.out.println(">>> Sample train schedule seeded successfully for: " + schedule.getTravelDate());
    }
}