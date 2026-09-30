# Ticket Booking Project - Full A to Z Explanation

This file explains the project from start to finish in a simple, practical way. It covers:

- what the project does
- what each package and file does
- how login and JWT work
- how booking flow works
- how backend and frontend are connected
- how database and security are wired together
- how all the files cooperate with each other

---

## 1. Project goal

This project is a railway ticket booking system backend written in Java using Spring Boot.

The system supports:

- user registration
- login
- train search by route and date
- seat availability display
- seat locking for a short time window
- ticket booking with passenger data
- mock payment
- booking cancellation
- admin features for train/schedule/fare management
- JWT-based authentication
- role-based authorization
- Docker deployment and CI setup

It is not only a CRUD project — it includes real business logic for seat locking, availability, PNR generation, and protection against duplicate seat allocation.

---

## 2. Project technology stack

The project uses:

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MySQL
- H2 database for tests
- Spring Security
- JWT (jjwt)
- Flyway for schema migration
- Actuator for health endpoints
- Springdoc OpenAPI / Swagger
- Lombok
- Maven Wrapper
- Docker + Docker Compose
- GitHub Actions

This stack makes it strong enough for a real working backend system with modern production-ready practices.

---

## 3. High-level architecture

The app has these main layers:

1. Presentation layer
   - controllers
   - REST endpoints

2. Service layer
   - business logic
   - validation
   - transaction management

3. Repository layer
   - Spring Data JPA repositories
   - database access

4. Persistence layer
   - entities / JPA models
   - database tables

5. Security layer
   - JWT filter
   - security config
   - user principal / authentication

6. Deployment layer
   - Dockerfile, docker-compose.yml
   - CI workflow
   - production config

The important idea is:

Client -> Controller -> Service -> Repository -> Database
and for protected APIs:

Client -> JWT Filter -> Security Config -> Controller -> Service -> Repository

---

## 4. Root project files and what they do

### pom.xml
This is the project configuration file.

It contains:

- Spring Boot parent info
- Java version
- dependencies for JPA, security, JWT, validation, tests, Swagger, Flyway, Actuator
- build plugins

It is the heart of the Maven build.

### mvnw / mvnw.cmd
These are the Maven wrapper scripts.

They let developers run Maven without installing Maven globally.

### Dockerfile
This builds the application as a Docker image.

It:

- uses Java 17 JDK for build stage
- downloads dependencies
- compiles the app
- packages it into a JAR
- creates a runtime image using JRE
- exposes port 8080

### docker-compose.yml
This starts the full stack:

- app container
- MySQL database container

It makes local development and deployment simple.

### .env.example
Example environment variables for local setup.

It contains values like:

- DB username/password
- JWT secret
- allowed origins
- server port

### .github/workflows/ci.yml
This pipeline runs automatically in GitHub Actions.

It does:

- checkout code
- setup Java 17
- run tests
- build jar

### PROJECT_STATUS.md
This file tracks what is implemented and what remains.

### process.md
This explains the project in architecture and process format.

### explanation.md
This file (what you are reading) is the full beginner-to-expert explanation of the project.

---

## 5. Main package: com.ticketbooking

The entire app follows package-based separation.

### 5.1 config
This package contains configuration and startup logic.

#### TicketBookingApplication.java
Main Spring Boot class.

It is the application bootstrap point.

#### DataInitializer.java
This runs on startup and seeds sample data.

It inserts:

- a train
- coaches
- seats
- schedule
- fare data
- initial admin user

This helps the app run immediately for demo/testing.

#### SecurityConfig.java
This is one of the most important files.

It configures:

- public routes
- protected routes
- admin-only routes
- JWT filter registration
- CORS support
- authentication provider
- password encoder

#### PendingPaymentExpiryScheduler.java
This scheduler handles expired pending bookings.

It ensures bookings not paid in time are expired and seat resources are released.

#### SeatAllocationInitializer.java
This initializes seat allocations for route schedules and seats.

It prepares seat states for booking and locking.

### 5.2 controller
This package contains REST endpoints.

#### UserController
Routes:

- POST /api/users/register
- POST /api/users/login

This is the entry point for authentication.

#### BookingController
Routes:

- POST /api/bookings
- GET /api/bookings/pnr/{pnrNumber}
- GET /api/bookings/user/{userId}
- POST /api/bookings/{bookingId}/pay
- POST /api/bookings/{bookingId}/cancel

This is the core of the booking flow.

#### SeatAllocationController
Routes:

- GET /api/schedules/{scheduleId}/seats
- POST /api/seat-allocations/lock

This is the seat availability and seat locking API layer.

#### ScheduleController
Routes:

- GET /api/schedules/search

Used to search train schedules by city and date.

#### AdminController
Routes:

- GET /api/admin/trains
- POST /api/admin/trains
- GET /api/admin/schedules
- POST /api/admin/schedules
- PUT /api/admin/schedules/{scheduleId}/fares

This manages admin operations.

#### GlobalExceptionHandler.java
This catches thrown exceptions and converts them into neat JSON error responses.

Example response shape:

- timestamp
- status
- error
- message
- path
- validationErrors

This ensures consistent API behavior.

### 5.3 dto
This package contains request/response models.

These are used so the controller layer receives structured input and returns structured output.

Examples:

- RegisterRequest
- LoginRequest
- UserResponse
- BookingRequest
- BookingResponse
- BookingDetailsResponse
- PassengerRequest
- SeatDTO
- SeatLockRequest
- SeatLockResponse
- ApiErrorResponse
- AdminTrainRequest
- AdminScheduleRequest
- AdminFareRequest

They are basically data contracts between client and backend.

### 5.4 model
This package contains database entities (JPA models).

#### User
Represents a user in the app.

Fields:

- id
- name
- phone
- cnic
- email
- password
- role

The `phone` is used as the login identifier.

#### Train
Represents a railway train.

Fields:

- id
- trainNumber
- name

#### Coach
Represents a coach in a train.

Fields:

- id
- coachNumber
- coachClass
- train

#### Seat
Represents a physical seat.

Fields:

- id
- seatNumber
- seatPosition
- coach

#### Schedule
Represents a schedule of a train on a route and date.

Fields:

- id
- train
- sourceCity
- destinationCity
- travelDate
- departureTime
- arrivalTime
- duration

#### ClassFare
Represents fare for a schedule by class.

Fields:

- id
- schedule
- coachClass
- fare

#### SeatAllocation
Represents the seat state for a schedule.

Fields:

- id
- schedule
- seat
- status
- lockToken
- lockExpiryTime

Status values:

- AVAILABLE
- LOCKED
- BOOKED

#### Booking
Represents a booking record.

Fields:

- id
- pnrNumber
- bookedBy (user)
- schedule
- totalAmount
- paymentStatus
- bookingStatus
- bookingTime
- paymentDeadline

#### Passenger
Represents each passenger in a booking.

Fields:

- id
- passengerName
- cnicOrBForm
- age
- gender
- seat
- booking

### 5.5 repository
This package contains Spring Data repositories.

Each repository extends JpaRepository and exposes methods for database query operations.

#### UserRepository
Used for:

- find by phone
- find by cnic
- exists by phone
- exists by cnic

#### ScheduleRepository
Used for:

- route and date search
- finding schedules by source/destination/date

#### SeatAllocationRepository
Used for:

- seat status lookup
- expired lock cleanup
- duplicate seat assignment prevention
- find by schedule + seat

#### BookingRepository
Used for:

- PNR lookup
- user booking history
- booking retrieval by id

---

## 6. Service layer details

This is where the business logic lives.

### UserService
Handles all user operations.

Responsibilities:

- check whether phone exists
- check whether cnic exists
- hash password using BCrypt
- create new customer account
- login user
- compare provided password with stored hash
- generate JWT token

Pseudo flow:

- client sends phone/password
- repository fetches user by phone
- password matches?
- if yes, create token and return `UserResponse`

### ScheduleService
Handles route search.

Responsibilities:

- query schedules for source/destination/date
- build train search results
- include class fares and available seats

### SeatAllocationService
Handles seated booking logic.

Responsibilities:

- list seats per schedule/class
- lock selected seats for a short time
- validate seat availability and lock state
- prevent duplicate seat lock attempts
- release expired locks

This is a critical service because it controls seat availability.

### BookingService
This is the main business engine of the project.

Responsibilities:

- validate lock token
- check passenger count vs seat count
- validate schedule/seat consistency
- generate unique PNR
- create booking record
- create passengers
- mark seat allocations as BOOKED
- process payment
- cancel booking
- release seats after cancellation

This service ensures data correctness and transaction safety.

### AdminService
Handles administrative features.

Responsibilities:

- verify if user is admin
- create train
- create schedule
- update fare for schedule/class

### CustomUserDetailsService
This is Spring Security integration.

It loads a `UserDetails` object from the database based on phone number.

Without this, Spring Security cannot authenticate using the app’s custom user model.

---

## 7. Security system in detail

This is a major part of the project and the key idea is: protect routes and authenticate the user before allowing them to use booking logic.

### 7.1 Login flow

This is the exact flow that happens when a user logs in.

Client sends:

- POST /api/users/login
- body: phone and password

Controller receives request and calls `UserService.login()`.

`UserService.login()` does:

- find user by phone
- compare entered password with stored hash using BCrypt
- if valid, generate JWT
- return `UserResponse` with token

Important: the raw password is never stored. Only the password hash is stored.

### 7.2 JWT generation

The logic is in `JwtService.java`.

`JwtService.generateToken(userDetails, userId)` creates a signed JWT using a secret key.

The JWT includes:

- subject: username (phone)
- claims: userId
- expiration date

This token is then returned to the client.

### 7.3 JWT filter

The JWT filter is `JwtAuthenticationFilter.java`.

When a request arrives:

- it checks the `Authorization` header
- if it starts with `Bearer `, it extracts the token
- it validates username and user details
- it loads the user from DB
- it checks token validity and expiry
- if valid, it stores authentication in `SecurityContextHolder`

This makes Spring Security know that the user is authenticated.

### 7.4 SecurityConfig rules

`SecurityConfig.java` acts like a gatekeeper.

Examples of rules:

- `/api/users/register` and `/api/users/login` are public
- search route is public
- booking routes are protected
- admin routes require `ROLE_ADMIN`

If a request lacks a valid token, it gets 401 Unauthorized.
If a user lacks admin role, it gets 403 Forbidden.

### 7.5 UserPrincipal

`UserPrincipal.java` wraps the database user as Spring `UserDetails`.

It provides:

- username
- password
- authorities
- account status information

This is required because Spring Security works with `UserDetails` abstraction.

---

## 8. Booking process explained step-by-step

This is the central flow of the app.

### Step 1: User searches for trains

Request:

- GET /api/schedules/search?from=Karachi Cantt&to=Lahore Jn&date=2026-10-01

Result:

- available train schedules
- train names and numbers
- fare details
- available seats per class

### Step 2: User selects a class and sees seats

Request:

- GET /api/schedules/{scheduleId}/seats?coachClass=ECONOMY

The service checks seat allocations and tells which seats are free and which are taken.

### Step 3: User locks desired seats

Request:

- POST /api/seat-allocations/lock

Body contains:

- scheduleId
- seatIds

Seat status changes from AVAILABLE to LOCKED.
A lock token is created and a short expiry time is assigned.

### Step 4: User submits booking

Request:

- POST /api/bookings

Body contains:

- userId
- scheduleId
- lockToken
- passengers

At this stage:

- booking service validates lock token
- checks that seats are actually locked
- validates passenger count with seat count
- computes total amount
- generates PNR
- creates booking and passenger records

### Step 5: Payment step

Request:

- POST /api/bookings/{bookingId}/pay

This simulates complete payment.

Booking payment status becomes PAID.

### Step 6: Cancellation

Request:

- POST /api/bookings/{bookingId}/cancel

This changes booking status to CANCELLED and releases seat allocations.

### Step 7: Expiry process

There are time-based rules:

- seat lock expires after a set timeout
- payment pending bookings also expire
- expired states are cleaned up so the seat becomes again available

This prevents stale locks from locking resources forever.

---

## 9. Security and booking interconnection

This is the real connection between app security and booking logic.

A user cannot directly book a ticket unless they are authenticated.

Protected flow:

1. User logs in and gets JWT
2. Client saves token
3. Client sends token on every protected request
4. JWT filter authenticates the user
5. Security config decides if route is allowed
6. Controller executes
7. Service validates business logic
8. Repository changes database

This means security is attached before reaching business logic.

---

## 10. What is happening in the database

The database schema is represented by the `model` package plus Flyway migration.

Main tables:

- users
- trains
- coaches
- seats
- schedules
- class_fares
- seat_allocations
- bookings
- passengers

Important relationships:

- a train has many coaches
- a coach has many seats
- a schedule belongs to a train
- a class fare belongs to a schedule
- a seat allocation belongs to a schedule and a seat
- a booking belongs to a user and a schedule
- passengers belong to a booking and a seat

This relational design is what makes seat allocation, fare lookup, and booking history possible.

---

## 11. How frontend and backend talk to each other

The frontend static files live in:

- `src/main/resources/static/app.js`
- `index.html` and other static assets

Frontend does the following:

- login/register using API calls
- store JWT in localStorage
- attach bearer token to protected requests
- search trains by route/date
- display class fares and available seats
- allow seat selection
- lock seats
- collect passenger info
- create ticket booking
- display booking confirmation and PNR
- show booking history

This is how the frontend and backend connect.

Example:

```
const headers = {
  "Content-Type": "application/json",
  "Authorization": `Bearer ${state.user.token}`
};
```

This is the exact mechanism used to authenticate with the backend.

---

## 12. Validation and exception handling

The app includes validation and global exception handling.

### Validation

DTOs use annotations such as:

- @NotBlank
- @Valid

This ensures required fields are not empty.

### Exception handling

`GlobalExceptionHandler.java` catches exceptions globally and returns a structured JSON response.

This is useful because:

- clients get meaningful messages
- frontend can show errors cleanly
- API response format stays consistent

Examples:

- invalid request body
- unauthorized access
- forbidden admin route
- resource not found
- business rule violation

---

## 13. Testing story

Project tests cover:

### BookingFlowIntegrationTest
This is the main business test.

It checks the happy path:

- register user
- login user
- find schedule
- view seats
- lock seat
- create booking
- pay booking
- cancel booking
- seat released again

### SecurityIntegrationTest
This verifies:

- login returns token
- protected endpoints reject unauthenticated users
- admin endpoints require admin role

### application-test.properties
This configures H2 in-memory database for testing.

This allows tests to run independently from MySQL.

---

## 14. Docker and deployment architecture

The app is built for containerization.

### Dockerfile
Builds the final JAR and runs it using Java 17 runtime image.

### docker-compose.yml
Starts:

- MySQL database
- Spring Boot app
- shared env variables

### application-prod.properties
Production profile uses environment variables instead of hardcoded values.

This is cleaner and more secure.

### CI workflow
GitHub Actions runs tests and build on every push/PR.

This is a strong automation step.

---

## 15. Environment variables and secrets

The app intentionally uses environment-driven configuration.

Important variables:

- DB_USERNAME
- DB_PASSWORD
- DB_URL
- JWT_SECRET
- JWT_EXPIRATION_MS
- APP_ALLOWED_ORIGINS
- SERVER_PORT
- SPRING_PROFILES_ACTIVE

This is a production-friendly pattern because secrets are not committed in source code.

---

## 16. Default admin user

The system seeds an admin account automatically during startup.

- phone: 03001234567
- password: Admin@123

This is useful for demo work.

In production, this should be changed after setup.

---

## 17. Request flow summary

### Flow A: Login and token generation

1. Client sends phone + password to `/api/users/login`
2. `UserController` receives it
3. `UserService.login()` checks DB user
4. BCrypt verifies password
5. `JwtService.generateToken()` creates JWT
6. JWT sent back to client
7. Client stores token for future requests

### Flow B: Booking with token

1. Client sends request with Authorization: Bearer token
2. `JwtAuthenticationFilter` extracts token
3. `JwtService` validates signature and expiry
4. `UserRepository` loads user by phone
5. `UserPrincipal` wraps as Spring `UserDetails`
6. SecurityContext marks user authenticated
7. Route is allowed or denied by `SecurityConfig`
8. Controller executes business logic
9. Service interacts with repository and database
10. Booking is saved, seat updated, response returned

---

## 18. Why the project is strong

The project is not a single-file app. It is designed as a layered application with real engineering concerns addressed:

- security
- validation
- exception handling
- transactions and business logic
- database design
- testing
- configuration management
- deployment support
- CI automation

This is exactly how a real product backend is usually structured.

---

## 19. Main dependency relationships

This is the conceptual chain:

- `UserController` uses `UserService`
- `UserService` uses `UserRepository` and `JwtService`
- `JwtAuthenticationFilter` uses `UserRepository` and `JwtService`
- `SecurityConfig` uses `JwtAuthenticationFilter` and `UserDetailsService`
- `BookingController` uses `BookingService`
- `BookingService` uses `BookingRepository`, `SeatAllocationRepository`, `ScheduleRepository`, `UserRepository`
- `ScheduleController` uses `ScheduleService`
- `SeatAllocationController` uses `SeatAllocationService`
- `AdminController` uses `AdminService`
- `AdminService` uses `UserRepository` and domain repositories

This is the main connectivity of the project.

---

## 20. Final understanding in one sentence

The project is a Spring Boot railway booking system where users authenticate with JWT, search and lock seats, create bookings with passenger data, process mock payments, manage admin resources, and use Flyway/Docker/CI to run in a modern real-world deployment setup.

---

## 21. Quick cheat sheet of important endpoints

Public:

- POST /api/users/register
- POST /api/users/login
- GET /api/schedules/search

Protected:

- GET /api/schedules/{scheduleId}/seats
- POST /api/seat-allocations/lock
- POST /api/bookings
- GET /api/bookings/user/{userId}
- GET /api/bookings/pnr/{pnrNumber}
- POST /api/bookings/{bookingId}/pay
- POST /api/bookings/{bookingId}/cancel

Admin only:

- GET /api/admin/trains
- POST /api/admin/trains
- GET /api/admin/schedules
- POST /api/admin/schedules
- PUT /api/admin/schedules/{scheduleId}/fares

Health:

- GET /actuator/health

Swagger:

- /swagger-ui.html
- /v3/api-docs

---

## 22. Final note

The project started as a functional booking app and was then hardened with:

- JWT authentication
- role-based security
- CORS limitation
- Flyway migration baseline
- Docker support
- CI pipeline
- environment variables for secrets

This is a strong progression from a demo app into a production-conscious application.

If you want, the next natural step is to add more business tests and then integrate a real payment gateway.
