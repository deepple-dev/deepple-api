CREATE TABLE IF NOT EXISTS admins
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

CREATE TABLE IF NOT EXISTS blocks
(
    blocked_id bigint not null,
    blocker_id bigint not null,
    created_at datetime(6) not null,
    id         bigint not null auto_increment,
    updated_at datetime(6),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS dating_exam_answer
(
    created_at  datetime(6) not null,
    id          bigint       not null auto_increment,
    question_id bigint       not null,
    updated_at  datetime(6),
    content     varchar(255) not null,
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS dating_exam_question
(
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    subject_id bigint       not null,
    updated_at datetime(6),
    content    varchar(255) not null,
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS dating_exam_subject
(
    is_public  bit          not null,
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    updated_at datetime(6),
    name       varchar(255) not null,
    type       varchar(50)  not null,
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS dating_exam_submit
(
    created_at datetime(6) not null,
    id         bigint       not null auto_increment,
    member_id  bigint       not null,
    subject_id bigint       not null,
    updated_at datetime(6),
    answers    varchar(255) not null,
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS device_registrations
(
    is_active          bit    not null,
    id                 bigint not null auto_increment,
    member_id          bigint,
    device_id          varchar(255),
    registration_token varchar(255),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS heart_purchase_options
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

CREATE TABLE IF NOT EXISTS heart_transactions
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

CREATE TABLE IF NOT EXISTS heart_usage_policies
(
    created_at       datetime(6) not null,
    id               bigint not null auto_increment,
    price            bigint,
    updated_at       datetime(6),
    gender           varchar(50),
    transaction_type varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS interview_answers
(
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    member_id   bigint,
    question_id bigint,
    updated_at  datetime(6),
    content     varchar(255),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS interview_questions
(
    is_public  bit    not null,
    created_at datetime(6) not null,
    id         bigint not null auto_increment,
    updated_at datetime(6),
    content    varchar(255),
    category   varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS likes
(
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    receiver_id bigint,
    sender_id   bigint,
    updated_at  datetime(6),
    level       varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS matches
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

CREATE TABLE IF NOT EXISTS member_hobbies
(
    member_id bigint not null,
    name      varchar(50)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS member_ideal_cities
(
    member_ideal_id bigint not null,
    cities          varchar(50)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS member_ideal_hobbies
(
    member_ideal_id bigint not null,
    hobbies         varchar(50)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS member_ideals
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

CREATE TABLE IF NOT EXISTS member_introductions
(
    created_at           datetime(6) not null,
    id                   bigint not null auto_increment,
    introduced_member_id bigint,
    member_id            bigint,
    updated_at           datetime(6),
    type                 varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS member_notification_preferences
(
    is_enabled        bit,
    member_id         bigint      not null,
    notification_type varchar(50) not null,
    primary key (member_id, notification_type)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS member_mission
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

CREATE TABLE IF NOT EXISTS members
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

CREATE TABLE IF NOT EXISTS missions
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

CREATE TABLE IF NOT EXISTS notification_preferences
(
    is_enabled_globally bit    not null,
    created_at          datetime(6) not null,
    deleted_at          datetime(6),
    id                  bigint not null auto_increment,
    member_id           bigint,
    updated_at          datetime(6),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS notification_templates
(
    is_active      bit    not null,
    id             bigint not null auto_increment,
    body_template  varchar(255),
    title_template varchar(255),
    type           varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS notifications
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

CREATE TABLE IF NOT EXISTS orders
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

CREATE TABLE IF NOT EXISTS profile_exchanges
(
    created_at   datetime(6) not null,
    id           bigint not null auto_increment,
    requester_id bigint not null,
    responder_id bigint not null,
    updated_at   datetime(6),
    status       varchar(50),
    primary key (id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS profile_images
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

CREATE TABLE IF NOT EXISTS reports
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

CREATE TABLE IF NOT EXISTS screenings
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

CREATE TABLE IF NOT EXISTS self_introductions
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

CREATE TABLE IF NOT EXISTS suspensions
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

CREATE TABLE IF NOT EXISTS warning_reasons
(
    warning_id  bigint not null,
    reason_type varchar(50)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS warnings
(
    is_critical bit    not null,
    admin_id    bigint,
    created_at  datetime(6) not null,
    id          bigint not null auto_increment,
    member_id   bigint,
    updated_at  datetime(6),
    primary key (id)
) engine=InnoDB;

CREATE INDEX IF NOT EXISTS idx_block_blocked_id
    ON blocks (blocked_id);

ALTER TABLE blocks
    ADD CONSTRAINT IF NOT EXISTS unique_blocker_id_blocked_id
    UNIQUE (blocker_id, blocked_id);

ALTER TABLE dating_exam_submit
    ADD CONSTRAINT IF NOT EXISTS uk_dating_exam_submit_member_subject
    UNIQUE (member_id, subject_id);

ALTER TABLE device_registrations
    ADD CONSTRAINT IF NOT EXISTS uk_member_id
    UNIQUE (member_id);

CREATE INDEX IF NOT EXISTS idx_receiver_id
    ON likes (receiver_id);

ALTER TABLE likes
    ADD CONSTRAINT IF NOT EXISTS UKr7tda4tud26t5ybryolk9105x
    UNIQUE (sender_id, receiver_id);

CREATE INDEX IF NOT EXISTS idx_responder_id
    ON matches (responder_id);

CREATE INDEX IF NOT EXISTS idx_requester_id_responder_id
    ON matches (requester_id, responder_id);

ALTER TABLE member_ideals
    ADD CONSTRAINT IF NOT EXISTS UK3e1lp5igxl6cxxdipej09phe6
    UNIQUE (member_id);

CREATE INDEX IF NOT EXISTS idx_deleted_at
    ON members (deleted_at);

ALTER TABLE members
    ADD CONSTRAINT IF NOT EXISTS UK99xbxdwmyun0ehfiwpbntlqs5
    UNIQUE (phone_number);

ALTER TABLE orders
    ADD CONSTRAINT IF NOT EXISTS UKx21diseqfw2bofpm4opvb6go
    UNIQUE (transaction_id, payment_method);

CREATE INDEX IF NOT EXISTS idx_responder_id
    ON profile_exchanges (responder_id);

CREATE INDEX IF NOT EXISTS idx_requester_id_responder_id
    ON profile_exchanges (requester_id, responder_id);

CREATE INDEX IF NOT EXISTS idx_member_id_is_primary
    ON profile_images (member_id, is_primary);

CREATE INDEX IF NOT EXISTS idx_member_id
    ON self_introductions (member_id);

ALTER TABLE suspensions
    ADD CONSTRAINT IF NOT EXISTS UKbe4hjk4livshyt1rnk4el2f7p
    UNIQUE (member_id);

CREATE INDEX IF NOT EXISTS idx_member_id
    ON warnings (member_id);

