# Ticket Booking Project - Complete Process and Architecture Overview

This document explains how the project was built, what each major component does, how files are connected, and how the request flow works end-to-end.

## 1. Project purpose

This project is a railway / train ticket booking backend built with Spring Boot. It supports:

- user registration and login
- train search by source, destination, and date
- seat availability listing by coach class
- temporary seat locking
- booking creation with passenger data
- mock payment flow
- booking cancellation
- admin train and schedule creation
- admin fare update
- JWT-based authentication and authorization
- database migrations via Flyway
- containerized deployment using Docker

The project is not just a toy API. It has been shaped as a working MVP with clear module separation, security, configuration hardening, and CI support.

---

## 2. Technology stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL
- H2 (for tests)
- Spring Security
- JWT (jjwt)
- Flyway
- Actuator
- Springdoc OpenAPI / Swagger
- Lombok
- Maven Wrapper
- Docker + Docker Compose
- GitHub Actions CI

---

## 3. Project structure overview

Root-level files:

- pom.xml
  - defines dependencies, Java version, build plugins
  - includes Spring Boot, Security, JWT, Flyway, Actuator, Swagger, test libs

- mvnw / mvnw.cmd
  - project wrapper for running Maven without global install

- Dockerfile
  - builds a runnable Spring Boot jar in a container

- docker-compose.yml
  - starts app + MySQL database together

- .env.example
  - sample environment variables for local/dev deployment

- .github/workflows/ci.yml
  - GitHub Actions pipeline for build and test verification

- PROJECT_STATUS.md
  - project roadmap and implementation status summary

- process.md
  - this document, explaining the whole project

Main source tree:

- src/main/java/com/ticketbooking
  - main application logic

- src/main/resources
  - application.properties, migration scripts, static frontend assets

- src/test/java/com/ticketbooking
  - integration and security tests

- src/test/resources/application-test.properties
  - H2 test database config

---

## 4. Main package architecture

The app is structured into logical modules under `com.ticketbooking`.

### 4.1 Application bootstrap

- `TicketBookingApplication.java`
  - main Spring Boot entry point
  - starts the application context and auto-configures beans

### 4.2 Configuration

- `config/DataInitializer.java`
  - runs at startup to seed initial train, coaches, seats, and schedule data
  - also creates a default admin user

- `config/SecurityConfig.java`
  - defines spring security rules
  - configures JWT filter, CORS, session policy, and endpoint authorization

- `config/PendingPaymentExpiryScheduler.java`
  - automatic scheduler to handle expired pending payment bookings

- `config/SeatAllocationInitializer.java`
  - initializes seat allocation records for schedules and seats

### 4.3 Controllers

Controllers are the HTTP interface of the API.

- `controller/UserController.java`
  - handles `POST /api/users/register`
  - handles `POST /api/users/login`

- `controller/BookingController.java`
  - handles booking creation, lookup, payment, cancellation

- `controller/SeatAllocationController.java`
  - handles seat listing and seat locking endpoints

- `controller/ScheduleController.java`
  - handles train search by route and date

- `controller/AdminController.java`
  - admin endpoints for creating trains, schedules, and updating fares

- `controller/GlobalExceptionHandler.java`
  - centralizes all API exceptions into structured JSON responses

### 4.4 Model layer

The domain model represents the real business objects.

- `model/User.java`
  - user identity details
  - fields: id, name, phone, cnic, email, password, role

- `model/Train.java`
  - train metadata like train number and name

- `model/Coach.java`
  - train coach metadata and relation to train

- `model/Seat.java`
  - seat itself, belonging to a coach

- `model/Schedule.java`
  - route + date + times + duration for a train schedule

- `model/ClassFare.java`
  - class-wise fare for a schedule, e.g. ECONOMY or AC_BUSINESS

- `model/SeatAllocation.java`
  - per-schedule per-seat status tracking
  - statuses: AVAILABLE, LOCKED, BOOKED
  - includes lock token and expiry

- `model/Booking.java`
  - booking record with PNR, user, schedule, amount, booking status, payment status

- `model/Passenger.java`
  - each passenger attached to a booking and a selected seat

### 4.5 DTOs

DTOs pass structured request/response data between controller and service.

Examples:

- `dto/RegisterRequest.java`
- `dto/LoginRequest.java`
- `dto/UserResponse.java`
- `dto/BookingRequest.java`
- `dto/BookingResponse.java`
- `dto/BookingDetailsResponse.java`
- `dto/PassengerRequest.java`
- `dto/SeatLockRequest.java`
- `dto/SeatLockResponse.java`
- `dto/SeatDTO.java`
- `dto/AdminTrainRequest.java`
- `dto/AdminScheduleRequest.java`
- `dto/AdminFareRequest.java`
- `dto/ApiErrorResponse.java`

These DTOs are not just wrappers; they provide validation rules as well.

### 4.6 Repositories

Repositories are Spring Data JPA interfaces.

- `repository/UserRepository.java`
  - login by phone
  - duplicate checks by phone/cnic

- `repository/TrainRepository.java`
  - train lookup and uniqueness checks

- `repository/CoachRepository.java`
  - coach queries

- `repository/SeatRepository.java`
  - seat access and queries

- `repository/ScheduleRepository.java`
  - route/date search

- `repository/ClassFareRepository.java`
  - fare lookup by schedule and class

- `repository/SeatAllocationRepository.java`
  - seat status queries, expired-lock cleanup, find by schedule + seat

- `repository/BookingRepository.java`
  - booking search by user, PNR, transaction tracking

### 4.7 Service layer

This is the real business logic layer.

- `service/UserService.java`
  - user registration
  - login
  - password hashing via BCrypt
  - JWT generation

- `service/ScheduleService.java`
  - search routes and compute available seats per class

- `service/SeatAllocationService.java`
  - list seats for a schedule
  - lock seats for 10 minutes
  - reject duplicate lock attempts
  - validate lock expiry and seat state

- `service/BookingService.java`
  - create booking from lock token and passenger list
  - validate seat/lock consistency
  - generate unique PNR
  - process payment
  - cancel booking
  - release seats on cancellation

- `service/AdminService.java`
  - admin authorization check
  - create train
  - create schedule
  - update fare

- `service/CustomUserDetailsService.java`
  - Spring Security loads user details from database using phone number

---

## 5. Security and auth design

### 5.1 Why security was added

The app originally relied on a user ID header (`X-User-Id`) for admin checks. That was not production-safe.

We upgraded it to JWT-based authentication.

### 5.2 Components

- `security/UserPrincipal.java`
  - implements Spring `UserDetails`
  - stores user ID, phone, password, role
  - creates ROLE_ prefixed authorities

- `security/JwtService.java`
  - generates tokens
  - validates tokens
  - extracts username and userId from JWT claims

- `security/JwtAuthenticationFilter.java`
  - reads `Authorization: Bearer <token>`
  - loads the user from DB
  - validates JWT and sets SecurityContext

### 5.3 Security rules

In `SecurityConfig.java`:

- public:
  - `/api/users/register`
  - `/api/users/login`
  - `/api/schedules/search`

- authenticated:
  - `/api/schedules/{scheduleId}/seats`
  - `/api/seat-allocations/lock`
  - `/api/bookings/**`

- admin-only:
  - `/api/admin/**`

This means only valid JWT users can access protected operations, and only ADMIN users can access admin routes.

### 5.4 CORS

The app now restricts cross-origin access instead of using wildcard `*`.

Configured via:

- `app.allowed-origins`
- `APP_ALLOWED_ORIGINS` environment variable

This is safer than allowing all origins.

---

## 6. Database and migration design

### 6.1 Data access strategy

The app uses JPA + Hibernate with entity mappings. Tables are created/updated via Hibernate in development/test, but Flyway is added for structured schema control.

### 6.2 Flyway

Migration script:

- `src/main/resources/db/migration/V1__init_schema.sql`

It creates the main schema for:

- users
- trains
- coaches
- seats
- schedules
- class_fares
- seat_allocations
- bookings
- passengers

`application.properties` enables Flyway:

- `spring.flyway.enabled=true`
- `spring.flyway.locations=classpath:db/migration`

This allows safer schema evolution as the project grows.

### 6.3 Why JPA and Flyway together

JPA handles object mapping and app-level data work.
Flyway handles controlled database schema versioning.
Together they give a cleaner production upgrade path.

---

## 7. Booking flow end-to-end

This is the key business flow of the project.

### Step 1: Search trains

Request:

- `GET /api/schedules/search?from=Karachi Cantt&to=Lahore Jn&date=2026-10-01`

Flow:

- `ScheduleController` receives request
- `ScheduleService` queries `ScheduleRepository`
- app finds matching schedules
- class fares and seat counts are computed
- response returns train info + class data + available seats

### Step 2: View seat map

Request:

- `GET /api/schedules/{scheduleId}/seats?coachClass=ECONOMY`

Flow:

- `SeatAllocationController`
- `SeatAllocationService.getSeats(...)`
- repository fetches seat allocation records for the schedule
- seats with `AVAILABLE` status are displayed to user

### Step 3: Lock seats

Request:

- `POST /api/seat-allocations/lock`

Body example:

- `scheduleId`
- `seatIds` list

Flow:

- service validates seats exist and are available
- rows are marked `LOCKED`
- unique lock token generated
- expiry timestamp set (10 minutes)
- response includes lock token and expiry time

Important: this prevents duplicate locking and race conditions using DB-level protection patterns.

### Step 4: Create booking

Request:

- `POST /api/bookings`

Body example:

- userId
- scheduleId
- lockToken
- passengers list

Flow:

- `BookingController` calls `BookingService.createBooking`
- service validates lock token
- verifies passenger count matches locked seats
- computes fare total
- generates unique PNR
- creates booking record and passenger rows in a transaction
- updates seat allocations to `BOOKED`
- booking status becomes `CONFIRMED`
- payment status becomes `PENDING`

### Step 5: Payment

Request:

- `POST /api/bookings/{bookingId}/pay`

Flow:

- service marks payment as `PAID`
- booking remains confirmed
- if payment is attempted twice, it rejects

### Step 6: Cancellation

Request:

- `POST /api/bookings/{bookingId}/cancel`

Flow:

- booking status becomes `CANCELLED`
- seat allocations are released to `AVAILABLE`
- cancellation logic prevents repeat cancellation

### Step 7: Expiry handling

The app also includes scheduler logic for expired payment and lock cleanup.

- `PendingPaymentExpiryScheduler` handles unpayed bookings after deadline
- `SeatAllocationInitializer` and lock cleanup logic ensures stale `LOCKED` allocations can be reset

This is important for correctness under concurrency and time-based conditions.

---

## 8. Frontend integration

The frontend files are located under:

- `src/main/resources/static/app.js`
- `src/main/resources/static/index.html` and related assets

The frontend uses the backend REST API directly.

Key frontend behaviors:

- login/register using `/api/users/login` and `/api/users/register`
- token saved in localStorage
- every protected API call sends `Authorization: Bearer <token>`
- admin pages call admin endpoints with the token
- user sees my-bookings and cancellations
- search + select train + choose class + lock seats + fill passengers + book

The static frontend is simple but complete for a working demo.

---

## 9. Validation and testing strategy

### 9.1 Test files

- `src/test/java/com/ticketbooking/BookingFlowIntegrationTest.java`
  - end-to-end flow: register -> login -> search -> seat list -> lock -> booking -> payment -> cancel -> seat release

- `src/test/java/com/ticketbooking/SecurityIntegrationTest.java`
  - login returns token
  - invalid access is rejected
  - admin endpoints require admin role

- `src/test/resources/application-test.properties`
  - H2 configuration for tests

### 9.2 Why tests matter here

This project has business logic that depends on:

- database state transitions
- lock validation
- payment rules
- ticket release logic
- security roles

So integration tests are essential. The tests confirm behavior is not just compile-time correct, but execution-time correct.

---

## 10. CI/CD and deployment setup

### 10.1 GitHub Actions

File:

- `.github/workflows/ci.yml`

Runs:

- JDK 17 setup
- `./mvnw test -q`
- `./mvnw -q -DskipTests package`

This ensures build and tests run on every push and PR.

### 10.2 Docker

Files:

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`

Docker sets up:

- app container for the Spring Boot backend
- MySQL container for database

This makes local environment setup easy and closer to production.

### 10.3 Production config

File:

- `src/main/resources/application-prod.properties`

It loads environment variables for:

- database URL/username/password
- JWT secret
- allowed origins
- health exposure
- Flyway

This is the environment-based configuration pattern we used.

---

## 11. Environment variables used

These are critical for production and local deployments.

- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_URL`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `APP_ALLOWED_ORIGINS`
- `SERVER_PORT`
- `SPRING_PROFILES_ACTIVE`

Sample file:

- `.env.example`

Use these variables instead of hardcoding secrets into code or config files.

---

## 12. Default admin account

The app seeds a default admin during startup.

- phone: `03001234567`
- password: `Admin@123`

This is helpful during testing and demo use, but in a real production deployment this should be replaced or rotated after initial setup.

---

## 13. Key file relationships (how everything connects)

This section shows the system wiring clearly.

### Authentication flow

- `UserController` receives login/register request
- `UserService` validates user and hashes password
- `UserRepository` fetches or saves the user
- `JwtService` generates a JWT token
- `UserResponse` includes the token
- `JwtAuthenticationFilter` reads the Bearer token on next requests
- `CustomUserDetailsService` loads user details from DB using phone
- `SecurityConfig` enforces allowed routes and roles

### Booking flow

- `ScheduleController` -> `ScheduleService` -> `ScheduleRepository`
- `SeatAllocationController` -> `SeatAllocationService` -> `SeatAllocationRepository`
- `BookingController` -> `BookingService` -> `BookingRepository`, `SeatAllocationRepository`, `UserRepository`
- `GlobalExceptionHandler` catches exceptions and returns uniform error JSON

### Admin flow

- `AdminController` -> `AdminService`
- `AdminService` checks user role from `UserRepository`
- if role is `ADMIN`, operations are allowed
- train, schedule, and fare creation happens through repositories

### Data initialization flow

- `DataInitializer` runs when app starts
- checks whether system data exists
- inserts train, coaches, seats, schedule, fare
- inserts default admin user

### Deployment flow

- `Dockerfile` builds jar from Maven
- `docker-compose.yml` launches app + MySQL
- `application-prod.properties` loads env variables
- `ci.yml` ensures build/test pass on GitHub

---

## 14. What was achieved in the final state

The project now includes:

- secure authentication and authorization
- public and protected endpoints
- role-based access for admins
- environment-based configuration
- Flyway schema baseline
- health endpoint
- CORS restriction
- deployment-ready Docker config
- CI pipeline
- automated tests for booking and security

This makes it much closer to a real-world application rather than just a backend prototype.

---

## 15. Remaining recommended improvements

Even though the project is strong, the following are still natural future steps:

- more advanced controller/service test coverage
- expired-lock concurrency tests
- stronger admin reporting features
- more realistic payment integration
- production secrets manager integration
- more granular prod vs dev profiles
- containerized frontend deployment

---

## 16. Final summary

This project is a complete railway ticket booking MVP built around a clean Spring Boot architecture:

- models represent the business domain
- repos abstract persistence
- services hold business logic
- controllers expose REST APIs
- security protects access
- tests validate real behavior
- Docker and CI make deployment repeatable

The app is now structured in a way that is understandable, scalable, and production-oriented.

If you want to continue improving it, the next ideal focus is extended test coverage and possibly a real payment gateway integration.
