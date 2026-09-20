package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String coachNumber; // e.g. "Coach-1", "Coach-2"

    // e.g. "AC_BUSINESS", "ECONOMY", "AC_STANDARD"
    private String coachClass;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;
}