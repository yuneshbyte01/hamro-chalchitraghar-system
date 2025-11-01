package com.hamrochalchitraghar.system.model;

import com.hamrochalchitraghar.system.model.enums.BookingChannel;
import com.hamrochalchitraghar.system.model.enums.BookingStatus;
import com.hamrochalchitraghar.system.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    private String seatNo;
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    private BookingChannel channel;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private String cancelledBy;  // could hold "CUSTOMER", "STAFF", or "SYSTEM"

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}
