package com.hamrochalchitraghar.system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SeatSyncController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/seat-update")
    public void broadcastSeatUpdate(Map<String, Object> update) {
        // Expecting: { "showId": 1, "seatNo": "A1", "status": "LOCKED", "updatedBy": "USER" }
        Long showId = Long.parseLong(update.get("showId").toString());
        messagingTemplate.convertAndSend("/topic/seats/" + showId, update);
    }
}
