
DROP TABLE IF EXISTS Member;
DROP TABLE IF EXISTS Movie;
DROP TABLE IF EXISTS Room;
DROP TABLE IF EXISTS Seat;
DROP TABLE IF EXISTS Session;
DROP TABLE IF EXISTS Booking;

CREATE TABLE Member (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE,
    password TEXT,
    birth TEXT
);

CREATE TABLE Movie (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    classification TEXT,
    duration INTEGER,
    summary TEXT
);

CREATE TABLE Room (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    type TEXT
);

CREATE TABLE Seat (
    seat_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_uid INTEGER,
    row TEXT,
    seat_number INTEGER
);

CREATE TABLE Session (
    uid INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_uid INTEGER,
    room_uid INTEGER,
    start_time TEXT
);

CREATE TABLE Booking (
    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
    member_uid INTEGER,
    movie_uid INTEGER,
    room_uid INTEGER,
    start_time TEXT,
    row TEXT,
    seat_number INTEGER,
    ticket_type TEXT,
    price INTEGER,
    status TEXT DEFAULT 'active'
);
