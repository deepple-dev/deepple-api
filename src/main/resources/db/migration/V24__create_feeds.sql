create table feeds
(
    id              bigint       not null auto_increment,
    type            varchar(50)  not null,
    author_id       bigint       not null,
    status          varchar(50)  not null,
    content         varchar(255) not null,
    published_at    datetime(6),
    grade           varchar(50),
    background_id   bigint,
    text_x          double,
    text_y          double,
    text_rotation   double,
    text_scale      double,
    title           varchar(255),
    is_pinned       bit,
    admin_id        bigint,
    idempotency_key varchar(100),
    deleted_at      datetime(6),
    created_at      datetime(6)  not null,
    updated_at      datetime(6),
    primary key (id),
    index idx_feeds_author_id_id (author_id, id),
    unique key uk_feeds_author_id_idempotency_key (author_id, idempotency_key)
) engine=InnoDB;

create table feed_images
(
    id            bigint       not null auto_increment,
    feed_id       bigint       not null,
    image_id      bigint       not null,
    image_key     varchar(255) not null,
    thumbnail_key varchar(255) not null,
    display_order integer      not null,
    created_at    datetime(6)  not null,
    updated_at    datetime(6),
    primary key (id),
    index idx_feed_images_feed_id (feed_id)
) engine=InnoDB;

create table feed_poll_options
(
    id            bigint       not null auto_increment,
    feed_id       bigint       not null,
    content       varchar(255) not null,
    display_order integer      not null,
    created_at    datetime(6)  not null,
    updated_at    datetime(6),
    primary key (id),
    index idx_feed_poll_options_feed_id (feed_id)
) engine=InnoDB;

create table feed_target_grades
(
    feed_id bigint      not null,
    grade   varchar(50) not null,
    primary key (feed_id, grade)
) engine=InnoDB;
