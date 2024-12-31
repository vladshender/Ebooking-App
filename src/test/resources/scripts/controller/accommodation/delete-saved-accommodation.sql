DELETE FROM accommodations
WHERE id = (
    SELECT id FROM accommodations
    WHERE location = 'Dnipro, Ukraine'
    AND type = 'HOUSE'
    AND size = '60m'
);
