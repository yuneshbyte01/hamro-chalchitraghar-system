package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
                .filter(seat -> !seat.isBooked())                      // not booked
                .filter(seat -> seat.getLockedAt() == null ||          // not locked
                        seat.getLockedAt().isBefore(now.minusMinutes(10))) // or lock expired
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
            seatRepository.save(seat);
        }
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

        // Step 3: Fetch Seats to be booked
        List<Seat> requestedSeats = seatRepository.findByShowId(showId).stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                .toList();

        if (requestedSeats.isEmpty()) {
            throw new RuntimeException("No valid seats found for this show!");
        }

        // Step 4: Check if all seats are available
        LocalDateTime now = LocalDateTime.now();
        for (Seat seat : requestedSeats) {
            if (seat.isBooked()) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is already booked!");
            }
            if (seat.getLockedAt() != null && seat.getLockedAt().isAfter(now.minusMinutes(10))) {
                throw new RuntimeException("Seat " + seat.getSeatNo() + " is temporarily locked!");
            }
        }

        // Step 5: Lock and mark seats as booked
        for (Seat seat : requestedSeats) {
            seat.setBooked(true);
            seat.setLockedBy(channel.name());
            seat.setLockedAt(now);
            seatRepository.save(seat);
        }

        // Step 6: Create a Booking record
        Booking booking = new Booking();
        booking.setCustomer(customer); // maybe null for box office
        booking.setShow(show);
        booking.setBookingTime(now);
        booking.setChannel(channel);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setSeatNo(String.join(",", seatNumbers)); // simple seat list storage

        bookingRepository.save(booking);

        // Step 7: Return confirmation
        return booking;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Free seats
        List<String> seatNumbers = List.of(booking.getSeatNo().split(","));
        List<Seat> seats = seatRepository.findByShowId(booking.getShow().getId())
                .stream()
                .filter(seat -> seatNumbers.contains(seat.getSeatNo()))
                .toList();

        for (Seat seat : seats) {
            seat.setBooked(false);
            seat.setLockedBy(null);
            seat.setLockedAt(null);
            seatRepository.save(seat);
        }
    }
}
