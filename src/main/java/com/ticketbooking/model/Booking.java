package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pnrNumber; // e.g. "PNR-78219"

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User bookedBy;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    private Double totalAmount;
    private String paymentStatus; // "PENDING", "PAID"
    private String bookingStatus; // "CONFIRMED", "CANCELLED"
    private LocalDateTime bookingTime;
    private LocalDateTime paymentDeadline;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Passenger> passengers = new ArrayList<>();
}