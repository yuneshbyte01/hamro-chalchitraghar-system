-- Sample Customers
INSERT INTO customers (name, email, password, role)
VALUES ('John Doe', 'john@example.com', '$2a$12$h9aVYmAyA5I5mS0ew0dR7uZUQekB2TH9MeSRpf3tBqHRMZgzSKSY2', 'CUSTOMER');

-- Sample Movies
INSERT INTO movies (title, genre, duration, language, poster_url)
VALUES ('K G F Chapter 2', 'Action', 155, 'Nepali Dub', 'https://example.com/kgf2.jpg'),
       ('12th Fail', 'Drama', 147, 'Hindi', 'https://example.com/12thfail.jpg');

-- Sample Shows
INSERT INTO shows (movie_id, hall_no, show_time, price)
VALUES (1, 1, '2025-11-01 18:30:00', 350.00),
       (1, 1, '2025-11-01 21:30:00', 400.00),
       (2, 2, '2025-11-01 20:00:00', 300.00);

-- Sample Seats for show 1
INSERT INTO seats (show_id, seat_no, booked)
VALUES
    (1, 'A1', FALSE), (1, 'A2', FALSE), (1, 'A3', FALSE),
    (1, 'B1', FALSE), (1, 'B2', FALSE), (1, 'B3', FALSE);
