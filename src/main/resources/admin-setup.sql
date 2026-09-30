-- After registering an account, run this once to grant admin access.
-- Replace 1 with the selected user's id.
USE ticket_db;

UPDATE users SET role = 'ADMIN' WHERE id = 1;
