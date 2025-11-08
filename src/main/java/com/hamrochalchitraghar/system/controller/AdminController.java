package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final CustomerRepository customerRepository;
    private final ShowRepository showRepository;

    /** Admin Dashboard – Overview */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            long totalBookings = bookingRepository.count();
            long totalCustomers = customerRepository.count();
            long totalMovies = movieRepository.count();

            // Approx revenue = show.price × number of bookings
            Double totalRevenue = bookingRepository.findAll().stream()
                    .mapToDouble(b -> b.getShow().getPrice())
                    .sum();

            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalCustomers", totalCustomers);
            model.addAttribute("totalMovies", totalMovies);
            model.addAttribute("totalRevenue", totalRevenue);

            return "admin/admin-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load admin dashboard: " + e.getMessage());
            return "error-page";
        }
    }
}
