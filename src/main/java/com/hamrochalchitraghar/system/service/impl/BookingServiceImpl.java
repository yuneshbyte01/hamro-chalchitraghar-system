package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import com.hamrochalchitraghar.system.service.EmailService;
import com.hamrochalchitraghar.system.service.SeatBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final CustomerRepository customerRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final ErrorLogRepository errorLogRepository;
    private final EmailService emailService;
    private final SeatBroadcastService seatBroadcastService;

    /**
     * Fetch all available (non-booked, non-locked) seats for a show.
     */
    @Override
    public List<Seat> getAvailableSeats(Long showId) {
        List<Seat> seats = seatRepository.findByShowId(showId);
        LocalDateTime now = LocalDateTime.now();

        return seats.stream()
                .filter(seat -> !seat.isBooked())
                .filter(seat -> seat.getLockedAt() == null || seat.getLockedAt().isBefore(now.minusMinutes(10)))
                .toList();
    }

    /**
     * Temporarily lock selected seats (used during seat selection).
     */
    @Transactional
    public void lockSeats(Long showId, List<String> seatNumbers, String lockedBy) {
        List<Seat> seatsToLock = seatRepository.findByShowId(showId).stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                .filter(seat -> !seat.isBooked())
                .toList();

        LocalDateTime now = LocalDateTime.now();

        for (Seat seat : seatsToLock) {
            seat.setLockedBy(lockedBy);
            seat.setLockedAt(now);
        }
        seatRepository.saveAll(seatsToLock);
    }

    /**
     * Main booking workflow — validate seats, create booking, send email, broadcast WebSocket.
     */
    @Override
    @Transactional
    public Booking bookSeats(Long customerId, Long showId, List<String> seatNumbers, BookingChannel channel) {

        try {
            // 1️⃣ Validate Show
            Show show = showRepository.findById(showId)
                    .orElseThrow(() -> new RuntimeException("Show not found with ID: " + showId));

            // 2️⃣ Validate Customer (optional for BOX_OFFICE)
            Customer customer = null;
            if (channel == BookingChannel.ONLINE) {
                customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            }

            // 3️⃣ Fetch and Lock Seats (pessimistic lock)
            List<Seat> requestedSeats = seatRepository.findSeatsForUpdate(showId, seatNumbers)
                    .stream()
                    .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                    .toList();

            if (requestedSeats.isEmpty()) {
                throw new RuntimeException("No valid seats found for this show!");
            }

            // 4️⃣ Validate seats
            LocalDateTime now = LocalDateTime.now();
            for (Seat seat : requestedSeats) {
                if (seat.isBooked()) {
                    throw new RuntimeException("Seat " + seat.getSeatNo() + " is already booked!");
                }
                if (seat.getLockedAt() != null && seat.getLockedAt().isAfter(now.minusMinutes(10))) {
                    throw new RuntimeException("Seat " + seat.getSeatNo() + " is temporarily locked!");
                }
            }

            // 5️⃣ Mark seats as booked
            for (Seat seat : requestedSeats) {
                seat.setBooked(true);
                seat.setLockedBy(channel.name());
                seat.setLockedAt(now);
            }
            seatRepository.saveAll(requestedSeats);

            // 6️⃣ Create Booking Record
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setShow(show);
            booking.setBookingTime(now);
            booking.setChannel(channel);
            booking.setStatus(BookingStatus.BOOKED);
            booking.setSeatNo(String.join(",", seatNumbers));

            bookingRepository.save(booking);

            // 7️⃣ Send email confirmation
            try {
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                    emailService.sendBookingConfirmation(booking);
                    System.out.println("📧 Booking confirmation email sent to " + customer.getEmail());
                } else {
                    System.out.println("⚠️ No email address found for customer ID: " + customerId);
                }
            } catch (Exception e) {
                logError("EmailService", e);
            }

            // 8️⃣ Real-time broadcast (WebSocket)
            try {
                seatBroadcastService.sendSeatUpdate(showId, requestedSeats);
                System.out.println("📡 WebSocket broadcast sent for Show ID: " + showId);
            } catch (Exception e) {
                logError("SeatBroadcastService", e);
            }

            return booking;

        } catch (DataIntegrityViolationException e) {
            logError("BookingService", e);
            throw new RuntimeException("Seat already booked — please refresh and try again!");
        } catch (Exception e) {
            logError("BookingService", e);
            throw new RuntimeException("Error during booking: " + e.getMessage());
        }
    }

    /**
     * Cancel a booking and release seats.
     */
    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found!"));

            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new RuntimeException("Booking already cancelled!");
            }

            // Identify who cancelled
            String actor = (booking.getCustomer() != null) ? "CUSTOMER" : "SYSTEM";

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            booking.setCancelledBy(actor);
            booking.setCancellationReason("User requested cancellation");
            bookingRepository.saveAndFlush(booking);

            // Release seats
            List<String> seatNumbers = List.of(booking.getSeatNo().split(","));
            List<Seat> seats = seatRepository.findByShowId(booking.getShow().getId())
                    .stream()
                    .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                    .toList();

            for (Seat seat : seats) {
                seat.setBooked(false);
                seat.setLockedBy(null);
                seat.setLockedAt(null);
            }
            seatRepository.saveAll(seats);

            // Broadcast unlock event
            try {
                seatBroadcastService.sendSeatUpdate(booking.getShow().getId(), seats);
                System.out.println("📡 Seat unlock broadcast sent for cancelled booking ID: " + bookingId);
            } catch (Exception e) {
                logError("SeatBroadcastService", e);
            }

            System.out.println("🔁 Booking " + bookingId + " cancelled by " + actor + ", seats released: " + seatNumbers);

        } catch (Exception e) {
            logError("BookingService", e);
            throw new RuntimeException("Error cancelling booking: " + e.getMessage());
        }
    }

    /**
     * Centralized error logger for all booking operations.
     */
    private void logError(String source, Exception e) {
        ErrorLog log = ErrorLog.builder()
                .source(source)
                .message(e.getMessage())
                .stackTrace(getStackTraceSnippet(e))
                .timestamp(LocalDateTime.now())
                .build();
        errorLogRepository.save(log);
    }

    private String getStackTraceSnippet(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 2000) break; // avoid bloating DB
        }
        return sb.toString();
    }
}
