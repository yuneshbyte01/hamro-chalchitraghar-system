package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.model.*;
import com.hamrochalchitraghar.system.model.enums.Role;
import com.hamrochalchitraghar.system.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            long totalBookings = bookingRepository.count();
            long totalMovies = movieRepository.count();
            long totalCustomers = customerRepository.count();
            double totalRevenue = bookingRepository.findAll().stream()
                    .mapToDouble(b -> b.getShow().getPrice())
                    .sum();

            // Bookings per Movie
            Map<String, Long> movieBookings = bookingRepository.findAll().stream()
                    .collect(Collectors.groupingBy(b -> b.getShow().getMovie().getTitle(), Collectors.counting()));

            // Revenue per Movie
            Map<String, Double> movieRevenue = bookingRepository.findAll().stream()
                    .collect(Collectors.groupingBy(b -> b.getShow().getMovie().getTitle(),
                            Collectors.summingDouble(b -> b.getShow().getPrice())));

            // Top 5 Movies
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

            return "admin/admin-dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String movie,
            @RequestParam(required = false) String status,
            Model model) {

        List<Booking> bookings = bookingRepository.findAll();

        if (startDate != null && endDate != null) {
            bookings = bookings.stream()
                    .filter(b -> !b.getBookingTime().toLocalDate().isBefore(startDate)
                            && !b.getBookingTime().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        if (movie != null && !movie.isBlank()) {
            bookings = bookings.stream()
                    .filter(b -> b.getShow().getMovie().getTitle().equalsIgnoreCase(movie))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            bookings = bookings.stream()
                    .filter(b -> b.getStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("bookings", bookings);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedMovie", movie);
        model.addAttribute("selectedStatus", status);
        return "admin/admin-reports";
    }

    /** Export report as Excel */
    @GetMapping("/reports/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Bookings");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Movie", "Show Time", "Seats", "Price", "Status"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            List<Booking> bookings = bookingRepository.findAll();
            int rowIdx = 1;
            for (Booking b : bookings) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getShow().getMovie().getTitle());
                row.createCell(2).setCellValue(b.getShow().getShowTime().toString());
                row.createCell(3).setCellValue(b.getSeatNo());
                row.createCell(4).setCellValue(b.getShow().getPrice());
                row.createCell(5).setCellValue(b.getStatus().name());
            }

            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=booking_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error exporting Excel: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<Customer> users = customerRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "admin/admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role newRole) {
        Customer user = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        user.setRole(newRole);
        customerRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        customerRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/halls")
    public String hallOccupancy(Model model) {
        List<Show> shows = showRepository.findAll();

        List<Map<String, Object>> hallData = new ArrayList<>();
        for (Show show : shows) {
            long totalSeats = show.getSeats().size();
            long bookedSeats = show.getSeats().stream().filter(Seat::isBooked).count();
            double occupancyRate = totalSeats == 0 ? 0 : (bookedSeats * 100.0 / totalSeats);

            Map<String, Object> hall = new HashMap<>();
            hall.put("hallNo", show.getHallNo());
            hall.put("movie", show.getMovie().getTitle());
            hall.put("showTime", show.getShowTime());
            hall.put("bookedSeats", bookedSeats);
            hall.put("totalSeats", totalSeats);
            hall.put("occupancyRate", occupancyRate);

            hallData.add(hall);
        }

        model.addAttribute("halls", hallData);
        return "admin/admin-halls";
    }
}
