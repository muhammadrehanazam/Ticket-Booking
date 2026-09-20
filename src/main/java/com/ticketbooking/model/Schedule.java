package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    private String sourceCity;       // e.g. "Karachi Cantt"
    private String destinationCity;  // e.g. "Lahore Jn"
    private LocalDate travelDate;    // e.g. 2026-09-10
    private String departureTime;    // e.g. "15:00"
    private String arrivalTime;      // e.g. "10:20 (+1)"
    private String duration;         // e.g. "19 h 20 min"
}