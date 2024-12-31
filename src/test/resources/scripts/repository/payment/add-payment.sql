delete from payments where id = 1;
insert into payments (id, booking_id, session_id, session_url, expired_time, amount, status)
values (1,
        1,
        '2334',
        'kdfksdmfksflks',
        12344553,
        240,
        'PENDING'
);