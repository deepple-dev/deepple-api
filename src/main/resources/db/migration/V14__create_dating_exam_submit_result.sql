CREATE TABLE dating_exam_submit_result
(
    id                         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id                  BIGINT      NOT NULL,
    decisive_independent_count INT         NOT NULL DEFAULT 0,
    growing_running_mate_count INT         NOT NULL DEFAULT 0,
    devoted_romantic_count     INT         NOT NULL DEFAULT 0,
    realistic_shelter_count    INT         NOT NULL DEFAULT 0,
    dominant_personality_type  VARCHAR(50) NOT NULL,
    created_at                 DATETIME(6) NOT NULL,
    updated_at                 DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_dating_exam_submit_result_member UNIQUE (member_id)
);
