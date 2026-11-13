insert into member_accounts (id, phone_number, status, suspended_until, created_at, updated_at, deleted_at)
select m.id, m.phone_number, 'ACTIVE', null, m.created_at, m.updated_at, m.deleted_at
from members m
where m.phone_number is not null
  and not exists (select 1 from member_accounts a where a.id = m.id)
  and not exists (select 1 from member_accounts a where a.phone_number = m.phone_number);

update member_accounts a
    join members m on m.id = a.id
set a.deleted_at = m.deleted_at
where a.deleted_at is null
  and m.deleted_at is not null;

update member_accounts a
    join suspensions s on s.member_id = a.id
set a.status          = 'SUSPENDED',
    a.suspended_until = s.expire_at
where a.status = 'ACTIVE'
   or not (a.suspended_until <=> s.expire_at);

update member_accounts a
    left join suspensions s on s.member_id = a.id
set a.status          = 'ACTIVE',
    a.suspended_until = null
where a.status = 'SUSPENDED'
  and s.id is null;

alter table members
    drop index uk_members_phone_number,
    add index idx_members_phone_number (phone_number);
