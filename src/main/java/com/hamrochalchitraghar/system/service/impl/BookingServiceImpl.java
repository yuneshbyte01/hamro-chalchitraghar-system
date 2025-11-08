package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import com.hamrochalchitraghar.system.service.EmailService;
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
    private final EmailService emailService;

    @Override
    public List<Seat> getAvailableSeats(Long showId) {
        List<Seat> seats = seatRepository.findByShowId(showId);
        LocalDateTime now = LocalDateTime.now();

        return seats.stream()
                .filter(seat -> !seat.isBooked())
                .filter(seat -> seat.getLockedAt() == null || seat.getLockedAt().isBefore(now.minusMinutes(10)))
                .toList();
    }

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

    @Override
    @Transactional
    public Booking bookSeats(Long customerId, Long showId, List<String> seatNumbers, BookingChannel channel) {

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

        // 4️⃣ Validate each seat
        LocalDateTime now = LocalDateTime.now();
        for (Seat seat : requestedSeats) {
            if (seat.isBooked()) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is already booked!");
            }
            if (seat.getLockedAt() != null && seat.getLockedAt().isAfter(now.minusMinutes(10))) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is temporarily locked!");
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
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setShow(show);
        booking.setBookingTime(now);
        booking.setChannel(channel);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setSeatNo(String.join(",", seatNumbers));

        try {
            bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Seat already booked — please refresh and try again!");
        }

        // 7️⃣ Trigger Email Confirmation (Mission 4.1)
        try {
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                emailService.sendBookingConfirmation(booking);
                System.out.println("📧 Booking confirmation email sent to " + customer.getEmail());
            } else {
                System.out.println("⚠️ No email address found for customer ID: " + customerId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send booking confirmation email: " + e.getMessage());
        }

        return booking;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
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

        System.out.println("🔁 Booking " + bookingId + " cancelled by " + actor + ", seats released: " + seatNumbers);
    }
}
