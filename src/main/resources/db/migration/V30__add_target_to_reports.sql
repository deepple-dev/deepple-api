alter table reports
    add column target_type varchar(50),
    add column target_id   bigint;

update reports
set target_type = 'MEMBER',
    target_id   = reportee_id;

update reports
set reason = 'CONTACT_EXPOSURE'
where reason = 'CONTACT_IN_PROFILE';

alter table reports
    modify column target_type varchar(50) not null,
    modify column target_id   bigint      not null,
    add constraint uk_reports_target_type_target_id_reporter_id unique (target_type, target_id, reporter_id);
