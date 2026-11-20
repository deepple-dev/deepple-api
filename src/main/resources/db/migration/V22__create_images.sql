create table images
(
    id             bigint       not null auto_increment,
    owner_type     varchar(50)  not null,
    owner_id       bigint       not null,
    content_type   varchar(50)  not null,
    status         varchar(50)  not null,
    source_key     varchar(255) not null,
    image_key      varchar(255) not null,
    thumbnail_key  varchar(255) not null,
    failure_reason varchar(255),
    created_at     datetime(6)  not null,
    updated_at     datetime(6),
    primary key (id),
    constraint uk_images_source_key unique (source_key)
) engine=InnoDB;
