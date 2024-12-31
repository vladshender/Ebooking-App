delete from bookings where id in (1, 2, 3);
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status, is_deleted)
VALUES (
    1,
    '2025-01-27 14:00:00',
    '2025-01-28 11:00:00',
    1,
    1,
    'PENDING',
    false
);
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status, is_deleted)
VALUES (
    2,
    '2024-02-21 12:00:00',
    '2024-02-23 14:00:00',
    1,
    1,
    'CANCELED',
    false
);
INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status, is_deleted)
VALUES (
    3,
    '2024-01-30 14:00:00',
    '2024-01-31 11:00:00',
    1,
    1,
    'PENDING',
    false
);
