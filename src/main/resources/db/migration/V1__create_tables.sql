create table admins
(
    created_at      datetime(6) not null,
    deleted_at      datetime(6),
    id              bigint not null auto_increment,
    updated_at      datetime(6),
    comment         varchar(255),
    email           varchar(255),
    name            varchar(255),
    password        varchar(255),
    phone_number    varchar(255),
    approval_status varchar(50),
    role            varchar(50),
    primary key (id)
) engine=InnoDB;

create table blocks
(
    blocked_id bigint not null,
    blocker_id bigint not null,
    created_at datetime(6) not null,
    id         bigint not null auto_increment,
    updated_at datetime(6),
    primary key (id)
) engine=InnoDB;

create table dating_exam_answer
(
    created_at  datetime(6) not null,
    id          bigint       not null auto_increment,
    question_id bigint       not null,
    updated_at  datetime(6),
    content     varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table dating_exam_question
(
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    subject_id bigint       not null,
    updated_at datetime(6),
    content    varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table dating_exam_subject
(
    is_public  bit          not null,
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    updated_at datetime(6),
    name       varchar(255) not null,
    type       varchar(50)  not null,
    primary key (id)
) engine=InnoDB;

create table dating_exam_submit
(
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    member_id  bigint       not null,
    subject_id bigint       not null,
    updated_at datetime(6),
    answers    varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create table device_registrations
(
    is_active          bit    not null,
    id                 bigint not null auto_increment,
    member_id          bigint,
    device_id          varchar(255),
    registration_token varchar(255),
    primary key (id)
) engine=InnoDB;

create table heart_purchase_options
(
    amount     bigint,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    id         bigint not null auto_increment,
    price      bigint,
    updated_at datetime(6),
    name       varchar(255),
    product_id varchar(255),
    primary key (id)
) engine=InnoDB;

create table heart_transactions
(
    amount                 bigint,
    created_at             datetime(6) not null,
    id                     bigint not null auto_increment,
    member_id              bigint,
    mission_heart_balance  bigint,
    purchase_heart_balance bigint,
    updated_at             datetime(6),
    content                varchar(255),
    transaction_type       varchar(50),
    primary key (id)
) engine=InnoDB;

create table heart_usage_policies
(
    created_at       datetime(6) not null,
    id               bigint not null auto_increment,
    price            bigint,
    updated_at       datetime(6),
    gender           varchar(50),
    transaction_type varchar(50),
    primary key (id)
) engine=InnoDB;

create table interview_answers
(
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    member_id   bigint,
    question_id bigint,
    updated_at  datetime(6),
    content     varchar(255),
    primary key (id)
) engine=InnoDB;

create table interview_questions
(
    is_public  bit    not null,
    created_at datetime(6) not null,
    id         bigint not null auto_increment,
    updated_at datetime(6),
    content    varchar(255),
    category   varchar(50),
    primary key (id)
) engine=InnoDB;

create table likes
(
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    receiver_id bigint,
    sender_id   bigint,
    updated_at  datetime(6),
    level       varchar(50),
    primary key (id)
) engine=InnoDB;

create table matches
(
    created_at             datetime(6) not null,
    id                     bigint not null auto_increment,
    read_by_responder_at   datetime(6),
    requester_id           bigint,
    responder_id           bigint,
    updated_at             datetime(6),
    request_message        varchar(255),
    response_message       varchar(255),
    requester_contact_type varchar(50),
    responder_contact_type varchar(50),
    status                 varchar(50),
    primary key (id)
) engine=InnoDB;

create table member_hobbies
(
    member_id bigint not null,
    name      varchar(50)
) engine=InnoDB;

create table member_ideal_cities
(
    member_ideal_id bigint not null,
    cities          varchar(50)
) engine=InnoDB;

create table member_ideal_hobbies
(
    member_ideal_id bigint not null,
    hobbies         varchar(50)
) engine=InnoDB;

create table member_ideals
(
    max_age         integer,
    min_age         integer,
    created_at      datetime(6) not null,
    id              bigint not null auto_increment,
    member_id       bigint,
    updated_at      datetime(6),
    drinking_status varchar(50),
    religion        varchar(50),
    smoking_status  varchar(50),
    primary key (id)
) engine=InnoDB;

create table member_introductions
(
    created_at           datetime(6) not null,
    id                   bigint not null auto_increment,
    introduced_member_id bigint,
    member_id            bigint,
    updated_at           datetime(6),
    type                 varchar(50),
    primary key (id)
) engine=InnoDB;

create table member_notification_preferences
(
    is_enabled        bit,
    member_id         bigint      not null,
    notification_type varchar(50) not null,
    primary key (member_id, notification_type)
) engine=InnoDB;

create table member_mission
(
    attempt_count integer not null,
    is_completed  bit     not null,
    success_count integer not null,
    created_at    datetime(6) not null,
    id            bigint  not null auto_increment,
    member_id     bigint  not null,
    mission_id    bigint  not null,
    updated_at    datetime(6),
    primary key (id)
) engine=InnoDB;

create table members
(
    height                   integer,
    is_dating_exam_submitted bit    not null,
    is_profile_public        bit    not null,
    is_vip                   bit    not null,
    year_of_birth            integer,
    created_at               datetime(6) not null,
    deleted_at               datetime(6),
    id                       bigint not null auto_increment,
    mission_heart_balance    bigint,
    purchase_heart_balance   bigint,
    updated_at               datetime(6),
    kakao_id                 varchar(255),
    nickname                 varchar(255),
    phone_number             varchar(255),
    activity_status          varchar(50),
    city                     varchar(50),
    district                 varchar(50),
    drinking_status          varchar(50),
    gender                   varchar(50),
    grade                    varchar(50),
    highest_education        varchar(50),
    job                      varchar(50),
    mbti                     varchar(50),
    primary_contact_type     varchar(50),
    religion                 varchar(50),
    smoking_status           varchar(50),
    primary key (id)
) engine=InnoDB;

create table missions
(
    is_public        bit     not null,
    repeatable_count integer not null,
    required_attempt integer not null,
    rewarded_heart   integer not null,
    created_at       datetime(6) not null,
    id               bigint  not null auto_increment,
    updated_at       datetime(6),
    action_type      varchar(50),
    frequency_type   varchar(50),
    target_gender    varchar(50),
    primary key (id)
) engine=InnoDB;

create table notification_preferences
(
    is_enabled_globally bit    not null,
    created_at          datetime(6) not null,
    deleted_at          datetime(6),
    id                  bigint not null auto_increment,
    member_id           bigint,
    updated_at          datetime(6),
    primary key (id)
) engine=InnoDB;

create table notification_templates
(
    is_active      bit    not null,
    id             bigint not null auto_increment,
    body_template  varchar(255),
    title_template varchar(255),
    type           varchar(50),
    primary key (id)
) engine=InnoDB;

create table notifications
(
    created_at  datetime(6) not null,
    deleted_at  datetime(6),
    id          bigint not null auto_increment,
    read_at     datetime(6),
    receiver_id bigint,
    sender_id   bigint,
    updated_at  datetime(6),
    body        varchar(255),
    title       varchar(255),
    sender_type varchar(50),
    status      varchar(50),
    type        varchar(50),
    primary key (id)
) engine=InnoDB;

create table orders
(
    created_at     datetime(6) not null,
    id             bigint not null auto_increment,
    member_id      bigint,
    updated_at     datetime(6),
    transaction_id varchar(255),
    payment_method varchar(50),
    status         varchar(50),
    primary key (id)
) engine=InnoDB;

create table profile_exchanges
(
    created_at   datetime(6) not null,
    id           bigint not null auto_increment,
    requester_id bigint not null,
    responder_id bigint not null,
    updated_at   datetime(6),
    status       varchar(50),
    primary key (id)
) engine=InnoDB;

create table profile_images
(
    is_primary    bit    not null,
    profile_order integer,
    created_at    datetime(6) not null,
    id            bigint not null auto_increment,
    member_id     bigint,
    updated_at    datetime(6),
    url           varchar(255),
    primary key (id)
) engine=InnoDB;

create table reports
(
    admin_id    bigint,
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    reportee_id bigint,
    reporter_id bigint,
    updated_at  datetime(6),
    version     bigint,
    content     varchar(255),
    reason      varchar(50),
    result      varchar(50),
    primary key (id)
) engine=InnoDB;

create table screenings
(
    admin_id         bigint,
    created_at       datetime(6) not null,
    id               bigint not null auto_increment,
    member_id        bigint,
    updated_at       datetime(6),
    version          bigint,
    rejection_reason varchar(50),
    status           varchar(50),
    primary key (id)
) engine=InnoDB;

create table self_introductions
(
    is_opened  bit    not null,
    created_at datetime(6) not null,
    deleted_at datetime(6),
    id         bigint not null auto_increment,
    member_id  bigint,
    updated_at datetime(6),
    content    varchar(255),
    title      varchar(255),
    primary key (id)
) engine=InnoDB;

create table suspensions
(
    admin_id   bigint,
    created_at datetime(6) not null,
    expire_at  datetime(6),
    id         bigint not null auto_increment,
    member_id  bigint,
    updated_at datetime(6),
    status     varchar(50),
    primary key (id)
) engine=InnoDB;

create table warning_reasons
(
    warning_id  bigint not null,
    reason_type varchar(50)
) engine=InnoDB;

create table warnings
(
    is_critical bit    not null,
    admin_id    bigint,
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    member_id   bigint,
    updated_at  datetime(6),
    primary key (id)
) engine=InnoDB;

create index idx_block_blocked_id
    on blocks (blocked_id);

alter table blocks
    add constraint unique_blocker_id_blocked_id unique (blocker_id, blocked_id);

alter table dating_exam_submit
    add constraint uk_dating_exam_submit_member_subject unique (member_id, subject_id);

alter table device_registrations
    add constraint uk_member_id unique (member_id);

create index idx_receiver_id
    on likes (receiver_id);

alter table likes
    add constraint UKr7tda4tud26t5ybryolk9105x unique (sender_id, receiver_id);

create index idx_responder_id
    on matches (responder_id);

create index idx_requester_id_responder_id
    on matches (requester_id, responder_id);

alter table member_ideals
    add constraint UK3e1lp5igxl6cxxdipej09phe6 unique (member_id);

create index idx_deleted_at
    on members (deleted_at);

alter table members
    add constraint UK99xbxdwmyun0ehfiwpbntlqs5 unique (phone_number);

alter table orders
    add constraint UKx21diseqfw2bofpm4opvb6go unique (transaction_id, payment_method);

create index idx_responder_id
    on profile_exchanges (responder_id);

create index idx_requester_id_responder_id
    on profile_exchanges (requester_id, responder_id);

create index idx_member_id_is_primary
    on profile_images (member_id, is_primary);

create index idx_member_id
    on self_introductions (member_id);

alter table suspensions
    add constraint UKbe4hjk4livshyt1rnk4el2f7p unique (member_id);

create index idx_member_id
    on warnings (member_id);

alter table member_hobbies
    add constraint FKs3rargk4m22bjg2tjhpc0ctt7
        foreign key (member_id)
            references members (id);

alter table member_ideal_cities
    add constraint FKo4hxe41a3bnikkcgnidgvlnsv
        foreign key (member_ideal_id)
            references member_ideals (id);

alter table member_ideal_hobbies
    add constraint FKi1nsyie3isud83mj5j77grkgr
        foreign key (member_ideal_id)
            references member_ideals (id);

alter table member_notification_preferences
    add constraint FK7b63dmtnmrxxvjs21pwto5dt6
        foreign key (member_id)
            references notification_preferences (id);

alter table warning_reasons
    add constraint FKouk9oiltck8uud2at58kqrjlx
        foreign key (warning_id)
            references warnings (id);
