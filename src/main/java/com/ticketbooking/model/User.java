package com.ticketbooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String phone; // Main login identifier

    @Column(unique = true, nullable = false)
    private String cnic;        // e.g. 42101-xxxxxxx-x

    private String email;

    @Column(nullable = false)
    private String password;

    private String role;        // "CUSTOMER", "ADMIN"
}