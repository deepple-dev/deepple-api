create table if not exists test_entity
(
    created_at datetime(6) not null,
    id         bigint not null auto_increment,
    updated_at datetime(6),
    name       varchar(255),
    primary key (id)
);

create table if not exists soft_delete_test_entity
(
    created_at datetime(6) not null,
    deleted_at datetime(6),
    id         bigint not null auto_increment,
    updated_at datetime(6),
    primary key (id)
);
