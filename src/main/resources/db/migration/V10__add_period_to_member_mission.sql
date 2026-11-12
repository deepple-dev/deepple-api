alter table member_mission
    add column period varchar(20) not null default 'ALL';

update member_mission mm
    join missions m on m.id = mm.mission_id
set mm.period = date_format(mm.created_at, '%Y-%m-%d')
where m.frequency_type = 'DAILY';

alter table member_mission
    add constraint uk_member_mission_member_mission_period unique (member_id, mission_id, period);
