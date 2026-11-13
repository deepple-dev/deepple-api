create table member_accounts
(
    id              bigint       not null auto_increment,
    phone_number    varchar(255) not null,
    status          varchar(50)  not null,
    suspended_until datetime(6),
    created_at      datetime(6)  not null,
    updated_at      datetime(6),
    deleted_at      datetime(6),
    primary key (id),
    constraint uk_member_accounts_phone_number unique (phone_number)
) engine=InnoDB;

insert into member_accounts (id, phone_number, status, suspended_until, created_at, updated_at, deleted_at)
select m.id,
       m.phone_number,
       case
           when m.activity_status = 'SUSPENDED_PERMANENTLY' then 'SUSPENDED_PERMANENTLY'
           when m.activity_status = 'SUSPENDED_TEMPORARILY'
               and exists (select 1
                           from suspensions s
                           where s.member_id = m.id
                             and s.status = 'TEMPORARY'
                             and s.expire_at is not null) then 'SUSPENDED_TEMPORARILY'
           else 'ACTIVE'
           end,
       case
           when m.activity_status = 'SUSPENDED_TEMPORARILY' then (select max(s.expire_at)
                                                                 from suspensions s
                                                                 where s.member_id = m.id
                                                                   and s.status = 'TEMPORARY')
           end,
       m.created_at,
       m.updated_at,
       m.deleted_at
from members m
where m.phone_number is not null;
