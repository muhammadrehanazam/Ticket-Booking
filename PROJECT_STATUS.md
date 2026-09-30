# Ticket Booking Project - Status and Roadmap

## Project ka maqsad

Yeh project railway ticket booking system ka Spring Boot backend hai. User ka expected flow yeh hoga:

1. Source, destination aur travel date se train search karna.
2. Train ki class, fare aur available seats dekhna.
3. Passenger details ke sath seats select karna.
4. Temporary seat lock ke baad booking aur payment complete karna.
5. PNR se booking dekhna ya cancel karna.

## Technology stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- MySQL
- Lombok
- Maven wrapper (`mvnw` / `mvnw.cmd`)

## Abhi tak kya ban chuka hai

### 1. Application foundation

- Spring Boot application class maujood hai.
- Maven dependencies aur build plugins configured hain.
- MySQL datasource configuration `src/main/resources/application.properties` mein hai.
- JPA ka `ddl-auto=update` enabled hai, is liye entities ke mutabiq tables update ho sakte hain.

### 2. Database entities

Booking domain ke basic models bana diye gaye hain:

- `User`: customer/admin identity, phone, CNIC, email, password aur role.
- `Train`: train number aur name.
- `Coach`: train ke coaches aur coach class.
- `Seat`: coach ki seat number aur position.
- `Schedule`: route, travel date, departure/arrival time aur duration.
- `ClassFare`: schedule ke liye class-wise fare.
- `SeatAllocation`: schedule/seat ka `AVAILABLE`, `LOCKED` ya `BOOKED` status aur lock expiry.
- `Booking`: PNR, user, schedule, amount, payment/booking status aur booking time.
- `Passenger`: booking ke passengers aur assigned seats.

### 3. Repositories

Sabhi major entities ke Spring Data repositories available hain. In mein booking ko PNR, user ya schedule se dhoondna, schedule ko route/date se dhoondna, aur seat allocation ko schedule/seat ya expired lock ke basis par dhoondna shamil hai.

### 4. Train search API

`GET /api/schedules/search?from={source}&to={destination}&date={yyyy-MM-dd}`

- Source, destination aur travel date ke basis par schedules search hotay hain.
- Response mein train details, route, timing, duration, class fares aur available seats milti hain.
- Available seats calculate karte waqt `BOOKED` aur `LOCKED` allocations ko occupied count kiya jata hai.
- Example request `src/main/resources/booktrain.http` mein di hui hai.

### 5. Sample data

`DataInitializer` first run par sample data insert karta hai:

- Karakoram Express (`11UP`)
- Karachi Cantt se Lahore Jn route
- Economy: 10 seats, fare 3750
- AC Business: 6 seats, fare 10950
- Kal ki date ka sample schedule

### 6. Testing

- Basic Spring context test maujood hai: `TicketBookingApplicationTests`.
- Major MVP flow manually runtime par test ho chuka hai: search, seats, lock, duplicate lock, booking, payment, history, cancellation, repeat cancellation, auth aur validation errors.
- H2-based automated booking-flow integration test added: auth, search, seat listing, lock conflict, booking, payment, history, PNR, cancellation and seat release.

## Abhi kya pending hai

### Priority 1 - Booking ka core flow — complete

- [x] Seat availability/details API: `GET /api/schedules/{scheduleId}/seats?coachClass=ECONOMY`
- [x] Seat ko 10 minutes ke liye safely lock karna: `POST /api/seat-allocations/lock`
- [x] Expired `LOCKED` seats ko scheduled cleanup se `AVAILABLE` karna.
- [x] Passenger details ke sath booking create API: `POST /api/bookings`
- [x] Pessimistic database locking aur `(schedule_id, seat_id)` unique constraint se double allocation rokna.
- [x] Booking confirmation par unique PNR generate karna.
- [x] Booking, passengers aur seat allocation state ko transaction ke andar save karna.

### Priority 1 APIs ka istemal

Pehle seats dekhein:

```http
GET /api/schedules/1/seats?coachClass=ECONOMY
```

Selected seats ko 10 minutes ke liye lock karein:

```json
POST /api/seat-allocations/lock
{
  "scheduleId": 1,
  "seatIds": [1, 2]
}
```

Response ka `lockToken` booking request mein use hota hai:

```json
POST /api/bookings
{
  "userId": 1,
  "scheduleId": 1,
  "lockToken": "lock-token-from-previous-response",
  "passengers": [
    {
      "passengerName": "Ali Khan",
      "cnicOrBForm": "42101-1234567-1",
      "age": 30,
      "gender": "MALE"
    },
    {
      "passengerName": "Sara Khan",
      "cnicOrBForm": "42101-7654321-2",
      "age": 28,
      "gender": "FEMALE"
    }
  ]
}
```

Passenger count aur locked seat count same hona zaroori hai. Booking successful hone par seats `BOOKED`, payment `PENDING`, booking `CONFIRMED` aur response mein PNR milta hai.

MVP mein passenger creation separate API se nahi hoti. Passenger details booking request ke andar hi submit hoti hain.

### Priority 2 - Users aur security

- [x] Registration API: `POST /api/users/register`
- [x] Login API: `POST /api/users/login`
- [x] Password ko BCrypt se hash karna.
- [x] Spring Security + JWT authentication add karna.
- [x] Customer aur admin roles ke endpoint permissions define karna.
- [x] Basic request validation add karna: required fields, passenger age/gender aur booking rules.
- [x] Default admin bootstrap user add karna: phone `03001234567`, password `Admin@123`.

### Priority 3 - Payment aur booking management

- [x] MVP mock payment API: `POST /api/bookings/{bookingId}/pay`
- [x] Successful mock payment par `paymentStatus = PAID` karna.
- [x] User booking history API: `GET /api/bookings/user/{userId}`
- [x] PNR se booking details API: `GET /api/bookings/pnr/{pnrNumber}`
- [x] Basic booking cancellation API: `POST /api/bookings/{bookingId}/cancel`
- [x] Payment aur booking status transitions ko basic MVP rules ke sath enforce karna.
- [x] Unpaid confirmed bookings ko 10 minutes ke baad automatically expire karke seats release karna.

### Priority 4 - Admin features

- [x] Admin role check aur admin dashboard add karna.
- [x] Admin ke liye trains create karna.
- [x] Schedules create karna.
- [x] Class fares update API available karna.
- [ ] Coaches aur seats manage karna.
- [ ] Booking aur seat occupancy reports banana.
- [ ] Sample `DataInitializer` ko production data se separate karna.

### Priority 5 - Frontend

- [x] Search form: source, destination aur date.
- [x] Search results: train, timing, class, fare aur available seats.
- [x] Seat layout aur seat selection screen.
- [x] Passenger information form.
- [x] Login/register screens.
- [x] Payment, confirmation aur PNR display screen.
- [x] My bookings aur cancellation screen.
- [ ] Admin dashboard.

### Priority 6 - Quality, security aur deployment

- [x] Core service booking-flow integration test add karna.
- [x] Security integration test add karna: login, auth required, admin authorization.
- [ ] Full controller, repository, concurrency aur expired-lock test coverage add karna.
- [x] Consistent error response format aur global exception handler add karna.
- [x] API documentation ke liye OpenAPI/Swagger add karna.
- [x] Database migrations ke liye Flyway baseline add karna.
- [x] Secrets ko environment variables/secrets manager se manage karna.
- [x] CORS ko wildcard se hata kar configured frontend origins tak limit karna.
- [x] Health checks add karna via Spring Boot Actuator.
- [x] Docker/CI pipeline aur deployment configuration banana. (Dockerfile, docker-compose.yml, .env.example, application-prod.properties add ho gaye hain.)

## Suggested next steps

1. Automated controller/service tests add karna, especially booking, payment, cancellation aur seat-lock conflict scenarios.
2. OpenAPI/Swagger documentation aur production docs polish karna.
3. Admin APIs aur dashboard add karna.
4. Production environment config ko finalise karna: real DB credentials, JWT secret, allowed origins, secure secrets management.
5. Docker/CI pipeline aur deployment configuration banana.

## Latest completed updates

- Spring Security + JWT authentication integrate ho chuka hai.
- Admin aur customer route authorization enforce ho chuka hai.
- CORS allowlist configured hai.
- Actuator health endpoint enabled hai.
- Flyway migration baseline add ho chuka hai.
- Security integration tests aur booking-flow integration tests passed hain.

## Important current notes

- Search se lekar booking, mock payment, cancellation aur frontend seat-map flow tak MVP working hai.
- Sample schedule `LocalDate.now().plusDays(1)` par banta hai. Testing ke waqt request date current sample schedule ki date ke mutabiq rakhni hogi.
- `application.properties` ke database credentials environment variables se provide karne honge; credentials source code mein commit nahi karne.
- `target/` generated build output hai; source changes `src/` ke andar karni hain.
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Demo database reset: application stop karke `src/main/resources/demo-reset.sql` ko MySQL Workbench mein run karein, phir application restart karein. Is se runtime users/bookings/passengers/allocations remove honge aur seed train data preserve rahega.
