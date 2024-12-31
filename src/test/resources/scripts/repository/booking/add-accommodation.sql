delete from accommodations where id = 1;
insert into accommodations (id, type, location, size, amenities, daily_rate, availability, is_deleted)
VALUES (
    1,
    'HOUSE',
    'Kyiv, Ukraine',
    '120m',
    ARRAY['WiFi', 'Garage'],
    120,
    2,
    false
);
