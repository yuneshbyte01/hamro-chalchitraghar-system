package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final CustomerRepository customerRepository;

    /** Dashboard Overview */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            long totalBookings = bookingRepository.count();
            long totalMovies = movieRepository.count();
            long totalCustomers = customerRepository.count();

            double totalRevenue = bookingRepository.findAll().stream()
                    .mapToDouble(b -> b.getShow().getPrice())
                    .sum();

            // --- Movie Analytics (Bookings per Movie) ---
            Map<String, Long> movieBookings = bookingRepository.findAll().stream()
                    .collect(Collectors.groupingBy(b -> b.getShow().getMovie().getTitle(), Collectors.counting()));

            // --- Movie Revenue ---
            Map<String, Double> movieRevenue = bookingRepository.findAll().stream()
                    .collect(Collectors.groupingBy(b -> b.getShow().getMovie().getTitle(),
                            Collectors.summingDouble(b -> b.getShow().getPrice())));

            // Top 5 Movies by Bookings
            LinkedHashMap<String, Long> top5Movies = movieBookings.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));

            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalMovies", totalMovies);
            model.addAttribute("totalCustomers", totalCustomers);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("movieBookings", movieBookings);
            model.addAttribute("movieRevenue", movieRevenue);
            model.addAttribute("top5Movies", top5Movies);

            return "admin/admin-analytics";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading analytics: " + e.getMessage());
            return "error-page";
        }
    }
}
