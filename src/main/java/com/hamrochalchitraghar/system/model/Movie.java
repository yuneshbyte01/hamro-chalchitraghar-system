package com.hamrochalchitraghar.system.model;

import com.hamrochalchitraghar.system.model.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String genre;
    private int duration;
    private String language;
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Show> shows;
}
