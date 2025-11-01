package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowId(Long showId);
    List<Seat> findByShowIdAndBookedFalse(Long showId);
}
