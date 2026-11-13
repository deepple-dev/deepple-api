create table event_publication
(
    id                     binary(16)    not null,
    listener_id            varchar(512)  not null,
    event_type             varchar(512)  not null,
    serialized_event       varchar(4000) not null,
    publication_date       datetime(6)   not null,
    completion_date        datetime(6),
    last_resubmission_date datetime(6),
    completion_attempts    integer       not null,
    status                 varchar(20),
    primary key (id),
    index idx_event_publication_completion_date (completion_date)
) engine=InnoDB;
