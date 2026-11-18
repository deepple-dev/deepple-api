alter table notifications
    add column idempotency_key varchar(100);

update notifications
set idempotency_key = concat('legacy:', id)
where idempotency_key is null;

alter table notifications
    modify column idempotency_key varchar(100) not null,
    add constraint uk_notifications_idempotency_key unique (idempotency_key);

alter table device_registrations
    add column status_changed_at datetime(6);

alter table notification_preferences
    add column global_setting_changed_at datetime(6);
