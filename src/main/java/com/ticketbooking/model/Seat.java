package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber; // e.g. "1", "2", "3"

    // e.g. "LOWER_BERTH", "UPPER_BERTH", "WINDOW_SEAT", "AISLE_SEAT"
    private String seatPosition;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;
}