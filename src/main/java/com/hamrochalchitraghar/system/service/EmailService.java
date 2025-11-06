package com.hamrochalchitraghar.system.service;

import com.hamrochalchitraghar.system.model.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(Booking booking) {
        if (booking.getCustomer() == null || booking.getCustomer().getEmail() == null) {
            System.out.println("⚠️ No email associated with booking: " + booking.getId());
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("✅ Email sent to: " + to);
    }
}
