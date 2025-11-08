package com.hamrochalchitraghar.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "error_logs")
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;         // e.g., BookingService, WebSocket
    private String message;        // Error message
    private String stackTrace;     // Optional full trace
    private LocalDateTime timestamp;
}
