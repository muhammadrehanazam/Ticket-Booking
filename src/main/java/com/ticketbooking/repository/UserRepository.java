package com.ticketbooking.repository;

import com.ticketbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByCnic(String cnic);
    boolean existsByPhone(String phone);
    boolean existsByCnic(String cnic);
}