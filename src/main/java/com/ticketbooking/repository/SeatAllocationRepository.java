package com.ticketbooking.repository;

import com.ticketbooking.model.SeatAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {
    List<SeatAllocation> findByScheduleId(Long scheduleId);

    Optional<SeatAllocation> findByScheduleIdAndSeatId(Long scheduleId, Long seatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a from SeatAllocation a
            where a.schedule.id = :scheduleId and a.seat.id = :seatId
            """)
    Optional<SeatAllocation> findForUpdateByScheduleIdAndSeatId(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId);

    List<SeatAllocation> findByStatusAndLockExpiryTimeBefore(String status, LocalDateTime time);

    List<SeatAllocation> findByScheduleIdAndLockToken(Long scheduleId, String lockToken);

    @Modifying
    @Query("""
            update SeatAllocation a
            set a.status = 'AVAILABLE', a.lockToken = null, a.lockExpiryTime = null
            where a.status = 'LOCKED' and a.lockExpiryTime < :now
            """)
    int releaseExpiredLocks(LocalDateTime now);
}