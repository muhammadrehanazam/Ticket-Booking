-- Demo reset for MySQL. Run only when the application is stopped.
-- This keeps trains, coaches, seats, schedules and fares, and removes
-- runtime users, bookings, passengers and seat allocations.

USE ticket_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE passengers;
TRUNCATE TABLE bookings;
TRUNCATE TABLE seat_allocations;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- SeatAllocationInitializer recreates AVAILABLE allocations on next startup.
