CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    phone VARCHAR(255) NOT NULL,
    cnic VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT uk_users_cnic UNIQUE (cnic)
);

CREATE TABLE IF NOT EXISTS trains (
    id BIGINT NOT NULL AUTO_INCREMENT,
    train_number VARCHAR(255),
    name VARCHAR(255),
    CONSTRAINT pk_trains PRIMARY KEY (id),
    CONSTRAINT uk_trains_train_number UNIQUE (train_number)
);

CREATE TABLE IF NOT EXISTS coaches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coach_number VARCHAR(255),
    coach_class VARCHAR(255),
    train_id BIGINT NOT NULL,
    CONSTRAINT pk_coaches PRIMARY KEY (id),
    CONSTRAINT fk_coaches_train FOREIGN KEY (train_id) REFERENCES trains (id)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seat_number VARCHAR(255),
    seat_position VARCHAR(255),
    coach_id BIGINT NOT NULL,
    CONSTRAINT pk_seats PRIMARY KEY (id),
    CONSTRAINT fk_seats_coach FOREIGN KEY (coach_id) REFERENCES coaches (id)
);

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    train_id BIGINT NOT NULL,
    source_city VARCHAR(255),
    destination_city VARCHAR(255),
    travel_date DATE,
    departure_time VARCHAR(255),
    arrival_time VARCHAR(255),
    duration VARCHAR(255),
    CONSTRAINT pk_schedules PRIMARY KEY (id),
    CONSTRAINT fk_schedules_train FOREIGN KEY (train_id) REFERENCES trains (id)
);

CREATE TABLE IF NOT EXISTS class_fares (
    id BIGINT NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    coach_class VARCHAR(255),
    fare DOUBLE,
    CONSTRAINT pk_class_fares PRIMARY KEY (id),
    CONSTRAINT fk_class_fares_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (id)
);

CREATE TABLE IF NOT EXISTS seat_allocations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status VARCHAR(255),
    lock_token VARCHAR(36),
    lock_expiry_time DATETIME,
    CONSTRAINT pk_seat_allocations PRIMARY KEY (id),
    CONSTRAINT uk_schedule_seat UNIQUE (schedule_id, seat_id),
    CONSTRAINT fk_seat_allocations_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (id),
    CONSTRAINT fk_seat_allocations_seat FOREIGN KEY (seat_id) REFERENCES seats (id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pnr_number VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    total_amount DOUBLE,
    payment_status VARCHAR(255),
    booking_status VARCHAR(255),
    booking_time DATETIME,
    payment_deadline DATETIME,
    CONSTRAINT pk_bookings PRIMARY KEY (id),
    CONSTRAINT uk_bookings_pnr UNIQUE (pnr_number),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (id)
);

CREATE TABLE IF NOT EXISTS passengers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    passenger_name VARCHAR(255),
    cnic_or_b_form VARCHAR(255),
    age INT,
    gender VARCHAR(255),
    seat_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    CONSTRAINT pk_passengers PRIMARY KEY (id),
    CONSTRAINT fk_passengers_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
    CONSTRAINT fk_passengers_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
);
