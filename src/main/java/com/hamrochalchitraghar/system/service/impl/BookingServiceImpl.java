package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.*;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return seatRepository.findByShowIdAndBookedFalse(showId);
    }

    @Override
    @Transactional
    public Booking bookSeats(Long customerId, Long showId, List<String> seatNumbers, BookingChannel channel) {
        // TODO: Validate show and seats
        // TODO: Lock seats temporarily (in-memory or DB-level)
        // TODO: Create booking
        // TODO: Update seats as booked
        return null; // will return an actual Booking object after implementation
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        // TODO: Retrieve booking, set status CANCELLED, and free seat(s)
    }
}
