package com.hamrochalchitraghar.system.service;

import com.hamrochalchitraghar.system.model.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendSeatUpdate(Long showId, List<Seat> updatedSeats) {
        messagingTemplate.convertAndSend("/topic/seat-updates",
                Map.of("showId", showId, "updatedSeats", updatedSeats));
    }
}
