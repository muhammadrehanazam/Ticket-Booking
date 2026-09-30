package com.ticketbooking.service;

import com.ticketbooking.dto.*;
import com.ticketbooking.model.*;
import com.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClassFareRepository classFareRepository;

    public void requireAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "User not found"));
        if (!"ADMIN".equals(user.getRole())) {
            throw error(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    public List<Train> trains() {
        return trainRepository.findAll();
    }

    @Transactional
    public Train createTrain(AdminTrainRequest request) {
        if (trainRepository.findByTrainNumber(request.getTrainNumber()).isPresent()) {
            throw error(HttpStatus.CONFLICT, "Train number already exists");
        }
        return trainRepository.save(Train.builder()
                .trainNumber(request.getTrainNumber()).name(request.getName()).build());
    }

    @Transactional
    public Schedule createSchedule(AdminScheduleRequest request) {
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Train not found"));
        String duration = calculateDuration(request.getDepartureTime(), request.getArrivalTime());
        return scheduleRepository.save(Schedule.builder()
                .train(train).sourceCity(request.getSourceCity())
                .destinationCity(request.getDestinationCity()).travelDate(request.getTravelDate())
                .departureTime(request.getDepartureTime()).arrivalTime(request.getArrivalTime())
                .duration(duration).build());
    }

    public List<Schedule> schedules() {
        return scheduleRepository.findAll();
    }

    @Transactional
    public ClassFare upsertFare(Long scheduleId, AdminFareRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Schedule not found"));
        ClassFare fare = classFareRepository.findByScheduleIdAndCoachClass(scheduleId, request.getCoachClass())
                .orElseGet(() -> ClassFare.builder().schedule(schedule)
                        .coachClass(request.getCoachClass()).build());
        fare.setFare(request.getFare());
        return classFareRepository.save(fare);
    }

    private ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }

    private String calculateDuration(String departure, String arrival) {
        try {
            LocalTime departureTime = LocalTime.parse(departure);
            LocalTime arrivalTime = LocalTime.parse(arrival);
            long minutes = Duration.between(departureTime, arrivalTime).toMinutes();
            if (minutes <= 0) {
                minutes += 24 * 60;
            }
            return (minutes / 60) + " h " + (minutes % 60) + " min";
        } catch (DateTimeParseException exception) {
            throw error(HttpStatus.BAD_REQUEST, "Departure and arrival time must use HH:mm format");
        }
    }
}
