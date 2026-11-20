create table feed_backgrounds
(
    id            bigint       not null auto_increment,
    image_key     varchar(255) not null,
    thumbnail_key varchar(255) not null,
    display_order integer      not null,
    is_hidden     bit          not null,
    created_at    datetime(6)  not null,
    updated_at    datetime(6),
    primary key (id)
) engine=InnoDB;

insert into feed_backgrounds (id, image_key, thumbnail_key, display_order, is_hidden, created_at)
values
    (1, 'feed-backgrounds/1/v1/image.webp', 'feed-backgrounds/1/v1/thumbnail.webp', 1, false, utc_timestamp(6)),
    (2, 'feed-backgrounds/2/v1/image.webp', 'feed-backgrounds/2/v1/thumbnail.webp', 2, false, utc_timestamp(6)),
    (3, 'feed-backgrounds/3/v1/image.webp', 'feed-backgrounds/3/v1/thumbnail.webp', 3, false, utc_timestamp(6)),
    (4, 'feed-backgrounds/4/v1/image.webp', 'feed-backgrounds/4/v1/thumbnail.webp', 4, false, utc_timestamp(6)),
    (5, 'feed-backgrounds/5/v1/image.webp', 'feed-backgrounds/5/v1/thumbnail.webp', 5, false, utc_timestamp(6)),
    (6, 'feed-backgrounds/6/v1/image.webp', 'feed-backgrounds/6/v1/thumbnail.webp', 6, false, utc_timestamp(6)),
    (7, 'feed-backgrounds/7/v1/image.webp', 'feed-backgrounds/7/v1/thumbnail.webp', 7, false, utc_timestamp(6)),
    (8, 'feed-backgrounds/8/v1/image.webp', 'feed-backgrounds/8/v1/thumbnail.webp', 8, false, utc_timestamp(6)),
    (9, 'feed-backgrounds/9/v1/image.webp', 'feed-backgrounds/9/v1/thumbnail.webp', 9, false, utc_timestamp(6));
