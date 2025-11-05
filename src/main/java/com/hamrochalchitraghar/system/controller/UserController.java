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
public class UserController {

    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /** Homepage — List all movies */
    @GetMapping("/")
    public String home(Model model) {
        try {
            List<Movie> movies = movieRepository.findAll();
            model.addAttribute("movies", movies);
            return "user/index";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load movies.");
            return "error-page";
        }
    }

    /** Movie details + showtime */
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
            model.addAttribute("error", "Unexpected error occurred.");
            return "error-page";
        }
    }

    /** Seat selection page */
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
            model.addAttribute("error", "Error loading seat selection.");
            return "error-page";
        }
    }

    /** Confirm booking */
    @PostMapping("/shows/{id}/book")
    public String confirmBooking(
            @PathVariable Long id,
            @RequestParam Long customerId,
            @RequestParam List<String> seatNumbers,
            Model model) {
        try {
            Booking booking = bookingService.bookSeats(customerId, id, seatNumbers, BookingChannel.ONLINE);
            model.addAttribute("booking", booking);
            return "user/booking-confirmation-user";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error during booking process.");
            return "error-page";
        }
    }

    /** View all user bookings */
    @GetMapping("/bookings")
    public String viewBookings(@RequestParam Long customerId, Model model) {
        try {
            List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
            model.addAttribute("bookings", bookings);
            return "user/user-bookings";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load bookings.");
            return "error-page";
        }
    }

    /** Cancel booking */
    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId, Model model) {
        try {
            bookingService.cancelBooking(bookingId);
            return "redirect:/user/bookings";
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
