package com.ticketbooking.repository;

import com.ticketbooking.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachRepository extends JpaRepository<Coach, Long> {
    List<Coach> findByTrainId(Long trainId);
    List<Coach> findByTrainIdAndCoachClass(Long trainId, String coachClass);
}