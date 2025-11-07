package com.hamrochalchitraghar.system.service.impl;

import com.hamrochalchitraghar.system.model.Booking;
import com.hamrochalchitraghar.system.service.PrintService;
import org.springframework.stereotype.Service;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PrintServiceImpl implements PrintService {

    @Override
    public void printTicket(Booking booking) {
        try {
            String ticketContent = """
                    🎬 Hamro Chalchitraghar
                    ----------------------------
                    Movie: %s
                    Show Time: %s
                    Hall: %s
                    Seats: %s
                    Booking ID: %d
                    ----------------------------
                    Thank you for visiting!
                    """.formatted(
                    booking.getShow().getMovie().getTitle(),
                    booking.getShow().getShowTime(),
                    booking.getShow().getHallNo(),
                    booking.getSeatNo(),
                    booking.getId()
            );

            // Convert to byte stream for printer
            byte[] bytes = ticketContent.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            // Locate the default printer
            javax.print.PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
            if (printer == null) {
                System.err.println("⚠️ No printer found.");
                return;
            }

            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(inputStream, flavor, null);
            DocPrintJob job = printer.createPrintJob();
            job.print(doc, null);

            System.out.println("🖨️ Ticket printed successfully for booking ID: " + booking.getId());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to print ticket: " + e.getMessage());
        }
    }
}
