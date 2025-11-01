package com.hamrochalchitraghar.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shows")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private int hallNo;
    private LocalDateTime showTime;
    private double price;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    private List<Seat> seats;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
