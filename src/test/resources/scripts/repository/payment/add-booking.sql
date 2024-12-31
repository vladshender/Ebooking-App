delete from bookings where id = 1;
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status, is_deleted)
VALUES (
    1,
    '2025-01-20 14:00:00',
    '2025-01-22 14:00:00',
    1,
    1,
    'PENDING',
    false
);
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status, is_deleted)
VALUES (
    2,
    '2025-01-23 14:00:00',
    '2025-01-25 14:00:00',
    1,
    1,
    'PENDING',
    false
);