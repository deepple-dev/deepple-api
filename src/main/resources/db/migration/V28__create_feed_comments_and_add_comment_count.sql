create table feed_comments
(
    id         bigint       not null auto_increment,
    feed_id    bigint       not null,
    parent_id  bigint,
    author_id  bigint       not null,
    content    varchar(255) not null,
    status     varchar(50)  not null,
    deleted_at datetime(6),
    created_at datetime(6)  not null,
    updated_at datetime(6),
    primary key (id),
    index idx_feed_comments_feed_id_parent_id_id (feed_id, parent_id, id),
    index idx_feed_comments_parent_id_id (parent_id, id)
) engine=InnoDB;

alter table feed_counts
    add column comment_count bigint not null default 0;
