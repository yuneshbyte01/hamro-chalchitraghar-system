package com.hamrochalchitraghar.system.service;

import com.hamrochalchitraghar.system.model.Booking;
import com.hamrochalchitraghar.system.model.ErrorLog;
import com.hamrochalchitraghar.system.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Handles outgoing booking confirmation emails and logs any errors to ErrorLog.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ErrorLogRepository errorLogRepository;

    /**
     * Sends a confirmation email to the customer after booking.
     * Automatically logs any errors to the database for admin visibility.
     */
    public void sendBookingConfirmation(Booking booking) {
        if (booking.getCustomer() == null || booking.getCustomer().getEmail() == null) {
            logError("EmailService", "No email associated with booking ID: " + booking.getId(), null);
            return;
        }

        String to = booking.getCustomer().getEmail();
        String subject = "🎟️ Hamro Chalchitraghar Ticket Confirmation";

        String body = """
                Dear %s,
                
                Your booking has been successfully confirmed!
                
                🎬 Movie: %s
                🕒 Show Time: %s
                🏛️ Hall: %s
                💺 Seats: %s
                🧾 Booking ID: %d
                
                Thank you for choosing Hamro Chalchitraghar.
                Enjoy your movie!
                """.formatted(
                booking.getCustomer().getName(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getShowTime(),
                booking.getShow().getHallNo(),
                booking.getSeatNo(),
                booking.getId()
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + to);
        } catch (Exception e) {
            logError("EmailService", "Failed to send email to " + to, e);
        }
    }

    /**
     * Logs any email sending errors to the ErrorLog table.
     */
    private void logError(String source, String message, Exception e) {
        ErrorLog log = ErrorLog.builder()
                .source(source)
                .message(message + (e != null ? " | " + e.getMessage() : ""))
                .stackTrace(e != null ? getStackTraceSnippet(e) : null)
                .timestamp(LocalDateTime.now())
                .build();
        errorLogRepository.save(log);
    }

    private String getStackTraceSnippet(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 2000) break; // prevent DB overflow
        }
        return sb.toString();
    }
}
