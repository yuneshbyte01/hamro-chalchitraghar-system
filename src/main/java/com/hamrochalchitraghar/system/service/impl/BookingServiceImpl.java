package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
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

        // Step 1: Validate Show
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with ID: " + showId));

        // Step 2: Validate Customer (optional for BOX_OFFICE)
        Customer customer = null;
        if (channel == BookingChannel.ONLINE) {
            customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        }

        // Step 3: Fetch and Lock Seats
        List<Seat> requestedSeats = seatRepository.findSeatsForUpdate(showId, seatNumbers)
                .stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                .toList();

        if (requestedSeats.isEmpty()) {
            throw new RuntimeException("No valid seats found for this show!");
        }

        // Step 4: Validate Seats
        LocalDateTime now = LocalDateTime.now();
        for (Seat seat : requestedSeats) {
            if (seat.isBooked()) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is already booked!");
            }
            if (seat.getLockedAt() != null && seat.getLockedAt().isAfter(now.minusMinutes(10))) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is temporarily locked!");
            }
        }

        // Step 5: Mark Seats as Booked
        for (Seat seat : requestedSeats) {
            seat.setBooked(true);
            seat.setLockedBy(channel.name());
            seat.setLockedAt(now);
        }
        seatRepository.saveAll(requestedSeats);

        // Step 6: Create a Booking Record
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

        return booking;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));

        // 1️⃣ Check if already canceled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking already cancelled!");
        }

        // 2️⃣ Determine actor
        String actor = "SYSTEM"; // default
        if (booking.getCustomer() != null) actor = "CUSTOMER";
        // later you can inject Auth context to detect STAFF vs. CUSTOMER

        // 3️⃣ Update booking fields
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(actor);
        booking.setCancellationReason("User requested cancellation"); // or pass as param
        bookingRepository.saveAndFlush(booking);

        // 4️⃣ Release seats
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
