package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.BookingChannel;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class CustomerController {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final CustomerRepository customerRepository;

    /** 🎬 User Dashboard — List all movies */
    @GetMapping("/dashboard")
    public String home(Model model) {
        try {
            List<Movie> movies = movieRepository.findAll();
            model.addAttribute("movies", movies);
            return "user/user-dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load movies at the moment.");
            return "error-page";
        }
    }

    /** 🎞 Movie Details & Showtime */
    @GetMapping("/movies/{id}")
    public String movieDetails(@PathVariable Long id, Model model) {
        try {
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Movie not found"));
            List<Show> shows = showRepository.findByMovieId(id);
            model.addAttribute("movie", movie);
            model.addAttribute("shows", shows);
            return "user/movie-details";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unexpected error occurred while loading movie details.");
            return "error-page";
        }
    }

    /** 🪑 Seat Selection Page */
    @GetMapping("/shows/{id}")
    public String seatSelection(@PathVariable Long id, Model model) {
        try {
            Show show = showRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Show not found"));
            List<Seat> seats = bookingService.getAvailableSeats(id);
            model.addAttribute("show", show);
            model.addAttribute("seats", seats);
            return "user/seat-selection";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading seat selection page.");
            return "error-page";
        }
    }

    /** ✅ Confirm Booking (Online Channel) */
    @PostMapping("/shows/{id}/book")
    public String confirmBooking(
            @PathVariable Long id,
            @RequestParam Long customerId,
            @RequestParam List<String> seatNumbers,
            Model model) {
        try {
            // Validate customer first
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

            Booking booking = bookingService.bookSeats(customer.getId(), id, seatNumbers, BookingChannel.ONLINE);
            model.addAttribute("booking", booking);
            return "user/booking-confirmation-user";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error during booking confirmation.");
            return "error-page";
        }
    }

    /** 🧾 View All Bookings for Customer */
    @GetMapping("/bookings")
    public String viewBookings(@RequestParam Long customerId, Model model) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found."));
            List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
            model.addAttribute("customer", customer);
            model.addAttribute("bookings", bookings);
            return "user/user-bookings";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load your bookings.");
            return "error-page";
        }
    }

    /** ❌ Cancel a Booking */
    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId, @RequestParam Long customerId, Model model) {
        try {
            bookingService.cancelBooking(bookingId);
            return "redirect:/user/bookings?customerId=" + customerId;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error while cancelling booking.");
            return "error-page";
        }
    }
}
