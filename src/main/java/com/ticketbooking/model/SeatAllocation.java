package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_allocations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_seat", columnNames = {"schedule_id", "seat_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // "AVAILABLE", "LOCKED", "BOOKED"
    private String status;

    @Column(length = 36)
    private String lockToken;

    private LocalDateTime lockExpiryTime;
}