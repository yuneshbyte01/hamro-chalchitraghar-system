package com.hamrochalchitraghar.system.service;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.BookingChannel;
import java.util.List;

public interface BookingService {

    // Fetch all available seats for a specific show
    List<Seat> getAvailableSeats(Long showId);

    // Book seats for a customer (online or box office)
    Booking bookSeats(Long customerId, Long showId, List<String> seatNumbers, BookingChannel channel);

    // Cancel an existing booking
    void cancelBooking(Long bookingId);
}
