package com.hamrochalchitraghar.system.model;

import com.hamrochalchitraghar.system.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Full name for tickets/emails

    @Column(unique = true, nullable = false)
    private String email; // Used for login + notifications

    @Column(nullable = false)
    private String password; // Will be encoded later (Spring Security)

    @Enumerated(EnumType.STRING)
    private Role role; // USER, STAFF, ADMIN

    @Column(length = 15)
    private String phone; // Optional (for SMS alerts)

    private boolean active = true; // Admin can deactivate users

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Booking> bookings; // Relation with Booking entity
}
