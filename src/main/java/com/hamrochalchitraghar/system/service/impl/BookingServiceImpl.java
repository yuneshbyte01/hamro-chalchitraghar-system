package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import com.hamrochalchitraghar.system.service.EmailService;
import com.hamrochalchitraghar.system.service.SeatBroadcastService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
     * Fetch all currently available (not booked or locked) seats for a show.
     */
    @Override
    public List<Seat> getAvailableSeats(Long showId) {
        LocalDateTime now = LocalDateTime.now();
        return seatRepository.findByShowId(showId).stream()
                .filter(seat -> !seat.isBooked())
                .filter(seat -> seat.getLockedAt() == null || seat.getLockedAt().isBefore(now.minusMinutes(10)))
                .toList();
    }

    /**
     * Temporarily lock selected seats to prevent double booking during checkout.
     */
    @Transactional
    public void lockSeats(Long showId, List<String> seatNumbers, String lockedBy) {
        LocalDateTime now = LocalDateTime.now();
        List<Seat> seatsToLock = seatRepository.findByShowId(showId).stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                .filter(seat -> !seat.isBooked())
                .toList();

        for (Seat seat : seatsToLock) {
            seat.setLockedBy(lockedBy);
            seat.setLockedAt(now);
        }
        seatRepository.saveAll(seatsToLock);
    }

    /**
     * Core booking workflow — validates customer & show, confirms seats,
     * records booking, sends email, and triggers WebSocket updates.
     */
    @Override
    @Transactional
    public Booking bookSeats(Long customerId, Long showId, List<String> seatNumbers, BookingChannel channel) {

        LocalDateTime now = LocalDateTime.now();

        try {
            // 1️⃣ Validate Customer (for ONLINE)
            Customer customer = null;
            if (channel == BookingChannel.ONLINE) {
                customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
                if (!customer.isActive()) {
                    throw new RuntimeException("Customer account is deactivated. Please contact support.");
                }
            }

            // 2️⃣ Validate Show
            Show show = showRepository.findById(showId)
                    .orElseThrow(() -> new RuntimeException("Show not found with ID: " + showId));

            // 3️⃣ Fetch and Lock Seats (pessimistic lock)
            List<Seat> requestedSeats = seatRepository.findSeatsForUpdate(showId, seatNumbers)
                    .stream()
                    .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                    .toList();

            if (requestedSeats.isEmpty()) {
                throw new RuntimeException("No valid seats found for this show!");
            }

            // 4️⃣ Validate Seat Availability
            for (Seat seat : requestedSeats) {
                if (seat.isBooked()) {
                    throw new RuntimeException("Seat " + seat.getSeatNo() + " is already booked.");
                }
                if (seat.getLockedAt() != null && seat.getLockedAt().isAfter(now.minusMinutes(10))) {
                    throw new RuntimeException("Seat " + seat.getSeatNo() + " is temporarily locked. Please refresh.");
                }
            }

            // 5️⃣ Mark Seats as Booked
            for (Seat seat : requestedSeats) {
                seat.setBooked(true);
                seat.setLockedBy(channel.name());
                seat.setLockedAt(now);
            }
            seatRepository.saveAll(requestedSeats);

            // 6️⃣ Create Booking Record
            Booking booking = Booking.builder()
                    .customer(customer)
                    .show(show)
                    .seatNo(String.join(",", seatNumbers))
                    .bookingTime(now)
                    .channel(channel)
                    .status(BookingStatus.BOOKED)
                    .build();

            bookingRepository.save(booking);

            // 7️⃣ Send Email Confirmation
            boolean emailSent = false;
            try {
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                    emailService.sendBookingConfirmation(booking);
                    emailSent = true;
                } else {
                    System.out.println("⚠️ Skipped email: No valid customer email found.");
                }
            } catch (Exception e) {
                logError("EmailService", e, customerId);
            }

            // (Optional Tracking)
            if (!emailSent) {
                System.out.println("⚠️ Email delivery failed or skipped for booking ID " + booking.getId());
            }

            // 8️⃣ WebSocket Broadcast (Seat Update)
            try {
                seatBroadcastService.sendSeatUpdate(showId, requestedSeats);
                System.out.println("📡 WebSocket broadcast sent for Show ID: " + showId);
            } catch (Exception e) {
                logError("SeatBroadcastService", e, customerId);
            }

            return booking;

        } catch (DataIntegrityViolationException e) {
            logError("BookingService", e, customerId);
            throw new RuntimeException("One or more selected seats are no longer available. Please refresh and retry.");
        } catch (RuntimeException e) {
            logError("BookingService", e, customerId);
            throw e; // rethrow clean message
        } catch (Exception e) {
            logError("BookingService", e, customerId);
            throw new RuntimeException("Unexpected booking error: " + e.getMessage());
        }
    }

    /**
     * Cancel booking and release seats safely.
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

            // 1️⃣ Identify Canceller
            String actor = (booking.getCustomer() != null) ? "CUSTOMER" : "SYSTEM";

            // 2️⃣ Update Booking Status
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            booking.setCancelledBy(actor);
            booking.setCancellationReason("User requested cancellation");
            bookingRepository.saveAndFlush(booking);

            // 3️⃣ Release Seats
            List<String> seatNumbers = List.of(booking.getSeatNo().split(","));
            List<Seat> seats = seatRepository.findByShowId(booking.getShow().getId()).stream()
                    .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                    .toList();

            for (Seat seat : seats) {
                seat.setBooked(false);
                seat.setLockedBy(null);
                seat.setLockedAt(null);
            }
            seatRepository.saveAll(seats);

            // 4️⃣ Broadcast Seat Unlock Event
            try {
                seatBroadcastService.sendSeatUpdate(booking.getShow().getId(), seats);
                System.out.println("📡 Seat unlock broadcast sent for cancelled booking ID: " + bookingId);
            } catch (Exception e) {
                logError("SeatBroadcastService", e,
                        booking.getCustomer() != null ? booking.getCustomer().getId() : null);
            }

            System.out.println("🔁 Booking " + bookingId + " cancelled by " + actor + ".");

        } catch (Exception e) {
            logError("BookingService", e, null);
            throw new RuntimeException("Error cancelling booking: " + e.getMessage());
        }
    }

    /**
     * Centralized database-backed error logger with source & user context.
     */
    private void logError(String source, Exception e, Long customerId) {
        ErrorLog log = ErrorLog.builder()
                .source(source + (customerId != null ? " (Customer ID: " + customerId + ")" : ""))
                .message(e.getMessage())
                .stackTrace(getStackTraceSnippet(e))
                .timestamp(LocalDateTime.now())
                .build();
        errorLogRepository.save(log);
    }

    /**
     * Stack trace summarizer (limits to 2000 chars for DB safety).
     */
    private String getStackTraceSnippet(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 2000) break;
        }
        return sb.toString();
    }
}
