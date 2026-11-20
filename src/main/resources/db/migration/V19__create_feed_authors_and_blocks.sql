create table feed_authors
(
    id                          bigint      not null auto_increment,
    author_type                 varchar(50) not null,
    member_id                   bigint,
    nickname                    varchar(255),
    gender                      varchar(50),
    year_of_birth               integer,
    city                        varchar(50),
    mbti                        varchar(50),
    profile_image_url           varchar(255),
    personality_type            varchar(50),
    activity_status             varchar(50),
    grade                       varchar(50),
    profile_updated_at          datetime(6),
    profile_image_updated_at    datetime(6),
    personality_type_updated_at datetime(6),
    activity_status_updated_at  datetime(6),
    grade_updated_at            datetime(6),
    created_at                  datetime(6) not null,
    updated_at                  datetime(6),
    primary key (id),
    constraint uk_feed_authors_member_id unique (member_id)
) engine=InnoDB;

create table feed_blocks
(
    id         bigint      not null auto_increment,
    blocker_id bigint      not null,
    blocked_id bigint      not null,
    created_at datetime(6) not null,
    updated_at datetime(6),
    primary key (id),
    constraint uk_feed_blocks_blocker_id_blocked_id unique (blocker_id, blocked_id),
    index idx_feed_blocks_blocked_id_blocker_id (blocked_id, blocker_id)
) engine=InnoDB;

insert into feed_authors (author_type, nickname, created_at)
values ('OFFICIAL', 'deepple_official', utc_timestamp(6));

insert into feed_authors (author_type, member_id, nickname, gender, year_of_birth, city, mbti,
                          profile_image_url, personality_type, activity_status, grade,
                          profile_updated_at, profile_image_updated_at, personality_type_updated_at,
                          activity_status_updated_at, grade_updated_at, created_at)
select 'MEMBER',
       m.id,
       m.nickname,
       m.gender,
       m.year_of_birth,
       m.city,
       m.mbti,
       (select pi.url
        from profile_images pi
        where pi.member_id = m.id
          and pi.is_primary = true
        order by pi.profile_order, pi.id
        limit 1),
       r.dominant_personality_type,
       m.activity_status,
       m.grade,
       utc_timestamp(6),
       utc_timestamp(6),
       if(r.dominant_personality_type is null, null, utc_timestamp(6)),
       utc_timestamp(6),
       utc_timestamp(6),
       m.created_at
from members m
         left join dating_exam_submit_result r on r.member_id = m.id;

insert into feed_blocks (blocker_id, blocked_id, created_at)
select blocker_id, blocked_id, min(created_at)
from blocks
group by blocker_id, blocked_id;
