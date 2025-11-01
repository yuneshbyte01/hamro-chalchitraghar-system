package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByShowId(Long showId);
}
