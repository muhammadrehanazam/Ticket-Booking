# Ticket Booking

A railway ticket booking system built with Spring Boot. The project includes train and schedule search, seat availability, temporary seat locking, passenger booking, mock payment, cancellation, JWT authentication, role-based admin APIs, a static frontend, database migrations, Docker support, and CI.

## Features

- Search trains by source, destination, and travel date
- View class fares, coaches, seats, and availability
- Lock seats temporarily before confirming a booking
- Create bookings with passenger details and generate a PNR
- Mock payment flow and booking cancellation
- Customer registration and login with BCrypt password hashing
- JWT authentication for protected APIs
- `CUSTOMER` and `ADMIN` role-based authorization
- Admin APIs for trains, schedules, and class fares
- Automatic expiry of stale seat locks and unpaid bookings
- MySQL persistence with Flyway migration support
- Swagger/OpenAPI documentation and Actuator health checks
- Docker Compose setup for the application and MySQL
- GitHub Actions build and test workflow

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| Web/API | Spring Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL 8 |
| Security | Spring Security, JWT, BCrypt |
| Migrations | Flyway |
| API docs | Springdoc OpenAPI |
| Testing | JUnit, Spring Boot Test, MockMvc, H2 |
| Build | Maven Wrapper |
| Deployment | Docker, Docker Compose |

## Project Structure

```text
src/main/java/com/ticketbooking/
├── config/          Startup seed data, security, and scheduled jobs
├── controller/      REST endpoints and exception handling
├── dto/             Request and response objects
├── model/           JPA entities
├── repository/      Spring Data repositories
├── security/        JWT service, filter, and authenticated principal
└── service/         Booking, schedule, seat, admin, and user logic

src/main/resources/
├── db/migration/    Flyway SQL migrations
├── static/          Browser frontend
├── application.properties
└── application-prod.properties

src/test/
├── BookingFlowIntegrationTest.java
├── SecurityIntegrationTest.java
└── TicketBookingApplicationTests.java
```

For a complete architecture and request-flow explanation, see [`explanation.md`](explanation.md) and [`process.md`](process.md).

## Prerequisites

- Java 17 or later
- MySQL 8, or Docker Desktop
- Git

## Configuration

Copy the environment template and replace the values for your environment:

```powershell
Copy-Item .env.example .env
```

Important variables:

| Variable | Purpose |
|---|---|
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `DB_URL` | JDBC connection URL |
| `JWT_SECRET` | Long, random signing secret |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds |
| `APP_ALLOWED_ORIGINS` | Comma-separated frontend origins |
| `SERVER_PORT` | Application port |

Do not commit `.env`, real credentials, or production JWT secrets. The values in `.env.example` are development placeholders only.

## Run Locally with MySQL

Create a MySQL database named `ticket_db`, then set the database environment variables and start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The application is available at:

- Frontend: http://localhost:8080
- API base URL: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health

## Run with Docker Compose

Docker Compose starts MySQL and the application together:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Stop the services with:

```powershell
docker compose down
```

The MySQL data is stored in the `mysql-data` Docker volume. To remove the volume as well, use `docker compose down -v`.

## Authentication Flow

Register or log in to receive a JWT:

```http
POST /api/users/login
Content-Type: application/json

{
  "phone": "03009998877",
  "password": "secret123"
}
```

Send the returned token with protected requests:

```http
Authorization: Bearer <jwt-token>
```

Public endpoints include registration, login, and train search. Seat, lock, booking, payment, history, and cancellation operations require authentication. Routes under `/api/admin/**` require the `ADMIN` role.

The demo bootstrap admin is:

```text
Phone: 03001234567
Password: Admin@123
```

Change or remove this account before using the application in production.

## Core Booking Flow

1. Search schedules:

   ```http
   GET /api/schedules/search?from=Karachi%20Cantt&to=Lahore%20Jn&date=YYYY-MM-DD
   ```

2. View seats:

   ```http
   GET /api/schedules/{scheduleId}/seats?coachClass=ECONOMY
   ```

3. Lock selected seats:

   ```http
   POST /api/seat-allocations/lock
   ```

4. Create a booking with the returned `lockToken` and passenger details:

   ```http
   POST /api/bookings
   ```

5. Complete the MVP mock payment:

   ```http
   POST /api/bookings/{bookingId}/pay
   ```

6. Retrieve the booking by PNR or cancel it when allowed.

Sample requests are available in [`src/main/resources/booktrain.http`](src/main/resources/booktrain.http).

## Testing

Run the complete test suite:

```powershell
.\mvnw.cmd test
```

The tests include the booking lifecycle, authentication requirements, JWT login, and admin authorization. H2 is used for the automated test database.

Build the application:

```powershell
.\mvnw.cmd clean package
```

## Database and Migrations

The initial Flyway migration is located at [`src/main/resources/db/migration/V1__init_schema.sql`](src/main/resources/db/migration/V1__init_schema.sql). Flyway runs migrations when the application starts. `DataInitializer` also creates sample train data for local development.

For a clean demo reset, stop the application and run [`src/main/resources/demo-reset.sql`](src/main/resources/demo-reset.sql) against the development database before restarting it.

## CI

GitHub Actions is configured in [`.github/workflows/ci.yml`](.github/workflows/ci.yml). It runs the Maven build and tests on pushes and pull requests.

## Project Documentation

- [`PROJECT_STATUS.md`](PROJECT_STATUS.md) — current implementation status and roadmap
- [`process.md`](process.md) — project process and component connections
- [`explanation.md`](explanation.md) — detailed A-to-Z architecture and request-flow explanation

## Production Notes

This repository is a secure, deployment-ready MVP, not a complete production payment platform. Before production deployment:

- Use a secrets manager for database credentials and `JWT_SECRET`
- Rotate or remove the demo admin credentials
- Configure HTTPS/TLS and production CORS origins
- Use a managed MySQL database and backups
- Add a real payment gateway if required
- Expand concurrency, controller, repository, and expired-lock test coverage
- Separate development sample-data initialization from production startup
