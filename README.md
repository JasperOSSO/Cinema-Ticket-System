# Cinema Ticket System

A Java Swing desktop application for managing cinema ticket booking, member login, movie sessions, and admin operations. The project uses SQLite for persistence and JSON files for movie and room seat data.

## Overview

This project simulates a cinema ticketing system with two major user flows:

- Member flow: register, log in, browse movies, book tickets, view booking history, cancel tickets, and delete account
- Admin flow: log in with a fixed password, view bookings, manage movies, manage sessions, and check revenue

The application entry point is the Swing UI in [CinemaTicketSystem/src/com/cinema/ui/MainEntryFrame.java](CinemaTicketSystem/src/com/cinema/ui/MainEntryFrame.java).

## Features

### Member features
- Create an account with email, password, and birth date
- Log in with email/password
- View available movies and sessions
- Book seats in a selected room and time slot
- View upcoming tickets and booking history
- Cancel active bookings
- Delete account and related records

### Admin features
- Password-based login for employees
- Query booking records
- Add or remove movies from the available catalog
- Manage movie sessions and room assignments
- Review revenue/activity data

## Project structure

```text
OOP_Final_Project/
├── README.md
├── CinemaTicketSystem/
│   ├── schema.sql
│   ├── cinema.db
│   ├── data/
│   │   ├── movie_info.json
│   │   ├── small_room.json
│   │   └── big_room.json
│   ├── lib/
│   │   ├── json-20210307.jar
│   │   └── sqlite-jdbc-3.36.0.3.jar
│   ├── src/
│   │   └── com/
│   │       └── cinema/
│   │           ├── db/
│   │           │   ├── AllDataInit.java
│   │           │   └── DBInit.java
│   │           └── ui/
│   │               ├── MainEntryFrame.java
│   │               ├── employee/
│   │               │   ├── AdminBookingViewFrame.java
│   │               │   ├── AdminFrame.java
│   │               │   ├── AdminLoginFrame.java
│   │               │   ├── AdminMovieManagerFrame.java
│   │               │   ├── AdminRevenueFrame.java
│   │               │   └── AdminSessionManagerFrame.java
│   │               └── user/
│   │                   ├── BookingFrame.java
│   │                   ├── BookingHistoryFrame.java
│   │                   ├── LoginFrame.java
│   │                   ├── MovieListFrame.java
│   │                   ├── RegisterFrame.java
│   │                   ├── SessionListFrame.java
│   │                   ├── UserHistoryFrame.java
│   │                   └── UserHomeFrame.java
│   └── bin/
└── .git/
```

## Database design

The database schema is defined in [CinemaTicketSystem/schema.sql](CinemaTicketSystem/schema.sql) and includes the following tables:

- Member: user login data
- Movie: movie title, classification, duration, summary
- Room: room name and type
- Seat: seat map per room
- Session: movie showtime by room
- Booking: booking detail, ticket type, price, and status

## Tech stack

- Java 17+ (Swing GUI)
- SQLite JDBC driver
- org.json library for JSON parsing
- Desktop GUI application

## Setup and run

From the project root directory:

```bash
cd CinemaTicketSystem
mkdir -p bin
javac -cp "lib/sqlite-jdbc-3.36.0.3.jar:lib/json-20210307.jar" -d bin $(find src -name "*.java")
```

Initialize the SQLite schema:

```bash
java -cp "bin:lib/sqlite-jdbc-3.36.0.3.jar:lib/json-20210307.jar" com.cinema.db.DBInit
```

Load sample movie/seat/session data:

```bash
java -cp "bin:lib/sqlite-jdbc-3.36.0.3.jar:lib/json-20210307.jar" com.cinema.db.AllDataInit
```

Launch the application:

```bash
java -cp "bin:lib/sqlite-jdbc-3.36.0.3.jar:lib/json-20210307.jar" com.cinema.ui.MainEntryFrame
```

## Default access

### Admin login
- Password: `passwords`

### Member registration
- Member accounts are created from the user login screen by clicking the registration link.

## Notes

- The app stores the database file as `CinemaTicketSystem/cinema.db` in the project folder.
- The JSON files in the `data` directory populate movies and room seat layouts.
- The project is designed as a desktop GUI; it requires a desktop environment to display the Swing windows.

## License

This project is intended for academic or educational use as part of an Object-Oriented Programming final project.
