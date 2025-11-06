# 🎬 Hamro Chalchitraghar System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/yuneshbyte01/hamro-chalchitraghar-system)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A **cinema hall ticketing and management platform** built with Spring Boot, MySQL, and Thymeleaf — designed for both online customers and box-office staff, ensuring real-time synchronization of bookings and seat availability.

---

## ✨ Features

### 🎟 Online Booking Portal
- Browse currently running movies
- View showtimes and available seats
- Select and book seats securely
- Cancel or view booking history
- Upcoming: PDF e-tickets with QR code

### 🏢 Box-Office Module
- Manage walk-in ticket sales
- Real-time seat synchronization with online bookings
- Instant seat selection and printing support
- View and cancel offline bookings

### 🧑‍💼 Admin Dashboard *(Coming Soon)*
- Add, update, or remove movies and shows
- Manage halls, pricing, and staff accounts
- View occupancy and revenue reports

---

## 🛠️ Tech Stack

| Category | Technology |
|-----------|-------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x |
| **Frontend** | Thymeleaf + Bootstrap 5 |
| **Database** | MySQL 8 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Security** | Spring Security + BCrypt *(planned)* |
| **Build Tool** | Maven |
| **Server** | Embedded Tomcat |
| **IDE** | IntelliJ IDEA / Eclipse |

---

## 🧱 Architecture

Controller → Service → Repository → Database  
↓  
Thymeleaf Views

Both **online** and **box-office** modules share the same service and database layers, guaranteeing real-time updates across all booking channels.

---

## 🌐 Endpoints

| Module | URL | Description |
|--------|-----|-------------|
| **User** | `/user/` | View all movies |
| | `/user/movies/{id}` | Movie details & showtimes |
| | `/user/shows/{id}` | Seat selection & booking |
| | `/user/bookings?customerId=1` | View or cancel bookings |
| **Staff** | `/staff/dashboard` | Today’s shows |
| | `/staff/shows/{id}` | Walk-in seat selection |
| | `/staff/bookings` | Box-office bookings list |

---

## 🗄️ Data Structure

| Entity | Description |
|--------|-------------|
| **Customer** | Stores user credentials and details |
| **Movie** | Contains movie title, genre, and metadata |
| **Show** | Represents specific showtimes and halls |
| **Seat** | Seat availability and lock status |
| **Booking** | Records confirmed or cancelled bookings |

All relationships use **JPA/Hibernate** with foreign key constraints for data consistency.

---

## 🚀 Quick Start

### 1. Clone the Repository
git clone https://github.com/yuneshbyte01/hamro-chalchitraghar-system.git  
cd hamro-chalchitraghar-system

### 2. Configure Database
Update credentials in `src/main/resources/application.properties`:

spring.datasource.url=jdbc:mysql://localhost:3306/hamrochalchitraghar_db  
spring.datasource.username=root  
spring.datasource.password=your_password  
spring.jpa.hibernate.ddl-auto=update  
spring.jpa.show-sql=true

### 3. Run the Application
mvn spring-boot:run

Then visit:
- http://localhost:8080/user/ — Online Booking Portal
- http://localhost:8080/staff/dashboard — Box-Office Dashboard

---

## 📸 Screens (Preview)

| Module | Description |
|--------|-------------|
| 🎟 User Home | Lists all available movies |
| 🪑 Seat Selection | Interactive seat layout for each show |
| ✅ Booking Confirmation | Displays booked seats, time, and hall |
| 🏢 Staff Dashboard | Today’s shows for walk-in bookings |

---

## 🧾 Example Commit Log

feat(ui): implement user and staff controllers with Thymeleaf integration for booking and seat management

---

## 🧩 Upcoming Enhancements

- 📧 Email/SMS ticket delivery
- 💳 Payment integration with eSewa / Khalti
- 🧾 QR-code ticket validation
- 🔐 Secure role-based authentication
- 📊 Admin analytics dashboard

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -m 'Add new feature'`)
4. Push to GitHub (`git push origin feature/new-feature`)
5. Open a Pull Request

---

Made with ❤️ by [Yunesh Timsina](https://github.com/yuneshbyte01)  
Star this repository if you like this project! ⭐
