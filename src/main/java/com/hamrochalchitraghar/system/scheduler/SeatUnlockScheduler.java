package com.hamrochalchitraghar.system.scheduler;

import com.hamrochalchitraghar.system.model.Seat;
import com.hamrochalchitraghar.system.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SeatUnlockScheduler {

    private final SeatRepository seatRepository;

    // Runs every minute
    @Scheduled(fixedRate = 60000)
    public void unlockExpiredSeats() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(2);
        List<Seat> lockedSeats = seatRepository.findAll().stream()
                .filter(s -> !s.isBooked() && s.getLockedAt() != null && s.getLockedAt().isBefore(cutoff))
                .toList();

        if (!lockedSeats.isEmpty()) {
            lockedSeats.forEach(seat -> {
                seat.setLockedAt(null);
                seat.setLockedBy(null);
            });
            seatRepository.saveAll(lockedSeats);
            System.out.println("🔓 Auto-unlocked expired seats: " + lockedSeats.size());
        }
    }
}
