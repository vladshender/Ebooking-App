delete from accommodations where id in (1, 2);
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
insert into accommodations (id, type, location, size, amenities, daily_rate, availability, is_deleted)
VALUES (
    2,
    'CONDO',
    'Lviv, Ukraine',
    '55m',
    ARRAY['WiFi', 'Garage'],
    100,
    1,
    false
);
