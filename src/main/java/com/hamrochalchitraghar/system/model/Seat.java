package com.hamrochalchitraghar.system.model;

import com.hamrochalchitraghar.system.model.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    private String seatNo;

    private boolean booked;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;
}
