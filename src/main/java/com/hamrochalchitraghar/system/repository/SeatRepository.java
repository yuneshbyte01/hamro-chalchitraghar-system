package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Arrays;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.booked = false")
    List<Seat> findByShowIdAndBookedFalse(@Param("showId") Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.seatNo IN :seatNumbers")
    List<Seat> findSeatsForUpdate(@Param("showId") Long showId, @Param("seatNumbers") List<String> seatNumbers);

    List<Seat> findByShowId(Long showId);

}
