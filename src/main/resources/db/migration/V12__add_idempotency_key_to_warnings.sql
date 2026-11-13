alter table warnings
    add column idempotency_key varchar(100),
    add constraint uk_warnings_idempotency_key unique (idempotency_key);
