package com.hamrochalchitraghar.system.repository;

import com.hamrochalchitraghar.system.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByMovieId(Long movieId);
    List<Show> findByShowTimeBetween(LocalDateTime start, LocalDateTime end);
}
