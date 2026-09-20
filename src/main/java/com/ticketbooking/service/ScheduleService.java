package com.ticketbooking.service;

import com.ticketbooking.dto.ClassFareDTO;
import com.ticketbooking.dto.TrainSearchResponseDTO;
import com.ticketbooking.model.Coach;
import com.ticketbooking.model.Schedule;
import com.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassFareRepository classFareRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final SeatAllocationRepository seatAllocationRepository;

    public List<TrainSearchResponseDTO> searchTrains(String from, String to, LocalDate date) {
        List<Schedule> schedules = scheduleRepository
                .findBySourceCityIgnoreCaseAndDestinationCityIgnoreCaseAndTravelDate(from, to, date);

        List<TrainSearchResponseDTO> responseList = new ArrayList<>();

        for (Schedule schedule : schedules) {
            var fares = classFareRepository.findByScheduleId(schedule.getId());
            List<ClassFareDTO> fareDTOs = new ArrayList<>();

            for (var fare : fares) {
                // Coach class ke mutabiq total seats calculate karein
                List<Coach> coaches = coachRepository.findByTrainIdAndCoachClass(
                        schedule.getTrain().getId(),
                        fare.getCoachClass()
                );

                int totalSeatsInClass = coaches.stream()
                        .mapToInt(c -> seatRepository.findByCoachId(c.getId()).size())
                        .sum();

                // Kitni seats booked ya locked hain
                long occupiedSeats = seatAllocationRepository.findByScheduleId(schedule.getId()).stream()
                        .filter(alloc -> coaches.stream().anyMatch(c -> c.getId().equals(alloc.getSeat().getCoach().getId())))
                        .filter(alloc -> "BOOKED".equals(alloc.getStatus()) || "LOCKED".equals(alloc.getStatus()))
                        .count();

                int availableSeats = Math.max(0, totalSeatsInClass - (int) occupiedSeats);

                fareDTOs.add(ClassFareDTO.builder()
                        .coachClass(fare.getCoachClass())
                        .fare(fare.getFare())
                        .availableSeats(availableSeats)
                        .build());
            }

            responseList.add(TrainSearchResponseDTO.builder()
                    .scheduleId(schedule.getId())
                    .trainNumber(schedule.getTrain().getTrainNumber())
                    .trainName(schedule.getTrain().getName())
                    .fromStation(schedule.getSourceCity())
                    .toStation(schedule.getDestinationCity())
                    .travelDate(schedule.getTravelDate())
                    .departureTime(schedule.getDepartureTime())
                    .arrivalTime(schedule.getArrivalTime())
                    .duration(schedule.getDuration())
                    .classFares(fareDTOs)
                    .build());
        }

        return responseList;
    }
}