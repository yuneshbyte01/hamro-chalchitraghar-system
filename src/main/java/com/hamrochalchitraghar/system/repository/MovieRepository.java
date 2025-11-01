package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByGenre(String genre);
    List<Movie> findByStatus(com.hamrochalchitraghar.system.model.enums.MovieStatus status);
}
