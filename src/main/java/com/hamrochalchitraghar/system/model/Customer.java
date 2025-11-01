package com.hamrochalchitraghar.system.model;

import com.hamrochalchitraghar.system.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
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

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
