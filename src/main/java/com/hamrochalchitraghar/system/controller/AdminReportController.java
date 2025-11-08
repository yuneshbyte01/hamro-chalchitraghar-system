package com.hamrochalchitraghar.system.controller;

import com.hamrochalchitraghar.system.model.Booking;
import com.hamrochalchitraghar.system.repository.BookingRepository;
import com.hamrochalchitraghar.system.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReportController {

    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;

    /** Report page with filters */
    @GetMapping("/reports")
    public String showReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String movie,
            @RequestParam(required = false) String status,
            Model model) {

        List<Booking> bookings = bookingRepository.findAll();

        // Filter by date range
        if (startDate != null && endDate != null) {
            bookings = bookings.stream()
                    .filter(b -> !b.getBookingTime().toLocalDate().isBefore(startDate)
                            && !b.getBookingTime().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());
        }

        // Filter by movie
        if (movie != null && !movie.isBlank()) {
            bookings = bookings.stream()
                    .filter(b -> b.getShow().getMovie().getTitle().equalsIgnoreCase(movie))
                    .collect(Collectors.toList());
        }

        // Filter by status
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
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

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
}
