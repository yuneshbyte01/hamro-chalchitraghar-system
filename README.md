# 🎮 Hamro Chalchitraghar System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL-blue.svg)](https://www.mysql.com/)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/yuneshbyte01/hamro-chalchitraghar-system)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> A **cinema hall ticketing and management platform** built with Spring Boot, MySQL, and Thymeleaf — designed for both online customers and box-office staff, ensuring real-time synchronization of bookings and seat availability.

---

## ✨ Features

### 🎟 Online Booking Portal

* Browse currently running movies
* View showtimes and available seats
* Select and book seats securely
* Receive automatic email confirmations
* Cancel or view booking history

### 🏢 Box-Office Module

* Manage walk-in ticket sales
* Print tickets via POS printer
* Real-time seat synchronization with online users
* Cancel or reprint bookings instantly

### 📧 Notifications System

* **Email confirmation** sent to customers after booking
* **Seat status updates** reflected instantly across all clients
* Future support for SMS alerts

### 🔁 Real-Time Seat Sync

* Spring WebSocket integration keeps online and staff seat layouts always in sync
* Temporary seat locks prevent double-booking
* Auto-unlock scheduler releases inactive seat locks after timeout

### 🧑‍💼 Admin Dashboard *(Coming Soon)*

* Manage movies, shows, and halls
* Configure pricing, seats, and schedules
* Monitor sales and occupancy analytics

---

## 🛠️ Tech Stack

| Category       | Technology                  |
| -------------- | --------------------------- |
| **Language**   | Java 21                     |
| **Framework**  | Spring Boot 3.x             |
| **Frontend**   | Thymeleaf + Bootstrap 5     |
| **Database**   | MySQL 8                     |
| **ORM**        | Spring Data JPA (Hibernate) |
| **Messaging**  | Spring WebSocket (STOMP)    |
| **Email**      | JavaMailSender (Gmail SMTP) |
| **Printing**   | Java PrintService API       |
| **Build Tool** | Maven                       |
| **Server**     | Embedded Tomcat             |
| **IDE**        | IntelliJ IDEA / Eclipse     |

---

## 🧱 Architecture

Controller → Service → Repository → Database
↓
Thymeleaf Views (for User & Staff interfaces)

Both **online** and **box-office** modules share the same service and database layers, guaranteeing **real-time consistency** across all booking channels.

---

## 🌐 Endpoints

| Module    | URL                           | Description               |
| --------- | ----------------------------- | ------------------------- |
| **User**  | `/user/`                      | View all movies           |
|           | `/user/movies/{id}`           | Movie details + showtimes |
|           | `/user/shows/{id}`            | Seat selection & booking  |
|           | `/user/bookings?customerId=1` | View or cancel bookings   |
| **Staff** | `/staff/dashboard`            | Today’s shows             |
|           | `/staff/shows/{id}`           | Walk-in seat selection    |
|           | `/staff/bookings`             | Box-office booking list   |

---

## 🗄️ Data Structure

| Entity       | Description                                   |
| ------------ | --------------------------------------------- |
| **Customer** | Stores user details and contact info          |
| **Movie**    | Movie metadata (title, genre, duration, etc.) |
| **Show**     | Represents specific showtimes and halls       |
| **Seat**     | Seat number, lock status, and booking state   |
| **Booking**  | Records each booking and its status           |
| **ErrorLog** | Captures backend or seat conflict errors      |

All entities use **JPA/Hibernate** with foreign key constraints for data consistency.

---

## 🚀 Quick Start

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/yuneshbyte01/hamro-chalchitraghar-system.git
cd hamro-chalchitraghar-system
```

### 2️⃣ Configure Database

Update credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hamrochalchitraghar_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3️⃣ Mail Configuration

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 4️⃣ Run the Application

```bash
mvn spring-boot:run
```

Access in your browser:

* 🎟 User Portal → [http://localhost:8080/user/](http://localhost:8080/user/)
* 🏢 Box Office Dashboard → [http://localhost:8080/staff/dashboard](http://localhost:8080/staff/dashboard)

---

## 📸 Screens (Preview)

| Screen                 | Description                     |
| ---------------------- | ------------------------------- |
| 🎮 Home Page           | Lists all movies now showing    |
| 🧡 Seat Selection      | Interactive seat layout         |
| ✅ Booking Confirmation | Shows ticket info after booking |
| 🏢 Staff Dashboard     | Manage daily walk-in bookings   |
| 🔨 POS Ticket Print    | Local printer ticket output     |

---

## 🗾 Recent Major Commits

| Commit                                                                     | Description                      |
| -------------------------------------------------------------------------- | -------------------------------- |
| `feat(ui): implement user and staff controllers`                           | Base booking and seat management |
| `feat(email): integrate booking confirmation email`                        | Auto email to customers          |
| `feat(pos): add POS ticket printing for box-office`                        | Local printer support            |
| `feat(websocket): enable live seat synchronization`                        | Real-time seat lock and sync     |
| `feat(error-handling): add global exception handling and auto seat unlock` | Stability and resilience         |

---

## 🧩 Upcoming Enhancements

* 💳 Payment integration (eSewa / Khalti)
* 🧳 QR-code ticket validation
* 🔐 Secure role-based authentication
* 📊 Admin analytics dashboard
* 🌐 Deployment with Docker and CI/CD pipeline

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch:
   `git checkout -b feature/new-feature`
3. **Commit** your changes:
   `git commit -m 'Add new feature'`
4. **Push** to your branch:
   `git push origin feature/new-feature`
5. **Open** a Pull Request on GitHub

---

**Made with ❤️ by [Yunesh Timsina](https://github.com/yuneshbyte01)**
Star this repository if you like this project! ⭐
