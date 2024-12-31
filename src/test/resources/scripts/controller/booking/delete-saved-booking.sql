DELETE FROM bookings
WHERE id = (
    SELECT id FROM bookings
    WHERE check_in_date = '2025-01-13 14:00:00'
    AND check_out_date = '2025-01-15 14:00:00'
    AND accommodation_id = 1
    AND status = 'PENDING'
);
