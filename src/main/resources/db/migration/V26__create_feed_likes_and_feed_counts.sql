create table feed_likes
(
    id         bigint      not null auto_increment,
    feed_id    bigint      not null,
    member_id  bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (id),
    unique key uk_feed_likes_feed_id_member_id (feed_id, member_id),
    index idx_feed_likes_member_id_feed_id (member_id, feed_id)
) engine=InnoDB;

create table feed_counts
(
    feed_id    bigint      not null,
    like_count bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (feed_id)
) engine=InnoDB;
