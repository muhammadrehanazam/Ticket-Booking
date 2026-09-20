package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "class_fares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    private String coachClass; // e.g. "ECONOMY", "AC_BUSINESS"
    private Double fare;       // e.g. 3750.0, 10950.0
}