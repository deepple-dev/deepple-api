create table feed_votes
(
    id         bigint      not null auto_increment,
    feed_id    bigint      not null,
    member_id  bigint      not null,
    option_id  bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (id),
    unique key uk_feed_votes_feed_id_member_id (feed_id, member_id),
    index idx_feed_votes_member_id_feed_id (member_id, feed_id)
) engine=InnoDB;

create table feed_vote_counts
(
    option_id  bigint      not null,
    feed_id    bigint      not null,
    vote_count bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (option_id),
    index idx_feed_vote_counts_feed_id (feed_id)
) engine=InnoDB;
