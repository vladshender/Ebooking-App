SELECT setval(pg_get_serial_sequence('accommodations', 'id'), (SELECT max(id) FROM accommodations));
