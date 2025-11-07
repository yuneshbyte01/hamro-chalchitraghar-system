package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.BookingChannel;
import com.hamrochalchitraghar.system.repository.*;
import com.hamrochalchitraghar.system.service.BookingService;
import com.hamrochalchitraghar.system.service.PrintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final PrintService printService;

    /**
     * Dashboard — view today's shows
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            LocalDate today = LocalDate.now();
            List<Show> shows = showRepository.findAll()
                    .stream()
                    .filter(s -> s.getShowTime().toLocalDate().isEqual(today))
                    .toList();

            model.addAttribute("shows", shows);
            return "staff/staff-dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load today's shows.");
            return "error-page";
        }
    }

    /**
     * Seat selection for a specific show
     */
    @GetMapping("/shows/{id}")
    public String seatSelection(@PathVariable Long id, Model model) {
        try {
            Show show = showRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Show not found"));
            List<Seat> seats = seatRepository.findByShowId(id);

            model.addAttribute("show", show);
            model.addAttribute("seats", seats);
            return "staff/staff-seat-selection";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading seat layout.");
            return "error-page";
        }
    }

    /**
     * Confirm walk-in booking
     */
    @PostMapping("/shows/{id}/book")
    public String confirmBooking(@PathVariable Long id,
                                 @RequestParam List<String> seatNumbers,
                                 Model model) {
        try {
            Booking booking = bookingService.bookSeats(null, id, seatNumbers, BookingChannel.BOX_OFFICE);

            // Immediately print via POS printer
            printService.printTicket(booking);

            // Pass booking info to confirmation page
            model.addAttribute("booking", booking);
            return "staff/booking-confirmation-staff";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error during walk-in booking.");
            return "error-page";
        }
    }

    /**
     * View all box-office bookings
     */
    @GetMapping("/bookings")
    public String viewBookings(Model model) {
        try {
            List<Booking> bookings = bookingRepository.findAll()
                    .stream()
                    .filter(b -> b.getChannel() == BookingChannel.BOX_OFFICE)
                    .toList();

            model.addAttribute("bookings", bookings);
            return "staff/staff-bookings";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load box-office bookings.");
            return "error-page";
        }
    }

    /**
     * Cancel a booking
     */
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, Model model) {
        try {
            bookingService.cancelBooking(id);
            return "redirect:/staff/bookings";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error while cancelling booking.");
            return "error-page";
        }
    }

    /**
     * Print ticket preview (browser-based print page)
     */
    @GetMapping("/print/{id}")
    public String printTicket(@PathVariable Long id, Model model) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            model.addAttribute("booking", booking);
            return "staff/print-ticket";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading ticket for printing.");
            return "error-page";
        }
    }
}
