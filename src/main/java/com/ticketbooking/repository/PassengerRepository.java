package com.ticketbooking.repository;

import com.ticketbooking.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Passenger p WHERE p.cnicOrBForm = :idNumber")
    List<Passenger> findByCnicOrBform(@Param("idNumber") String idNumber);
}