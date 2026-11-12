create table heart_balances
(
    id               bigint      not null auto_increment,
    member_id        bigint      not null,
    mission_balance  bigint      not null,
    purchase_balance bigint      not null,
    created_at       datetime(6) not null,
    updated_at       datetime(6),
    primary key (id),
    constraint uk_heart_balances_member_id unique (member_id)
) engine=InnoDB;

create table heart_members
(
    id                bigint      not null auto_increment,
    member_id         bigint      not null,
    gender            varchar(50),
    is_vip            bit         not null,
    source_updated_at datetime(6) not null,
    created_at        datetime(6) not null,
    updated_at        datetime(6),
    primary key (id),
    constraint uk_heart_members_member_id unique (member_id)
) engine=InnoDB;

alter table heart_transactions
    rename column mission_heart_balance to mission_balance,
    rename column purchase_heart_balance to purchase_balance,
    add column idempotency_key varchar(100),
    add constraint uk_heart_transactions_type_idempotency_key unique (transaction_type, idempotency_key),
    add index idx_heart_transactions_member_id_id (member_id, id);

insert into heart_balances (member_id, mission_balance, purchase_balance, created_at)
select id, coalesce(mission_heart_balance, 0), coalesce(purchase_heart_balance, 0), created_at
from members;

insert into heart_members (member_id, gender, is_vip, source_updated_at, created_at)
select id, gender, is_vip, utc_timestamp(6), created_at
from members;
