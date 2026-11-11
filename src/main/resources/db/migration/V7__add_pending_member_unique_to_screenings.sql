create unique index uk_screenings_pending_member on screenings (
    (case when status = 'PENDING' then member_id end)
);
