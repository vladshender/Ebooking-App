SELECT setval(pg_get_serial_sequence('bookings', 'id'), (SELECT max(id) FROM bookings));
