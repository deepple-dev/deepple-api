-- ============================================================
-- 02. 소개 데이터 생성
-- ============================================================
-- 이성 회원간 소개만 가능 (남성↔여성)

SET
@INTROS_PER_MEMBER = 30; -- 회원당 소개 수

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE member_introductions;
SET
FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 소개 시퀀스 테이블 생성 (1~30)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tmp_intro_seq;
CREATE TABLE tmp_intro_seq
(
    n INT PRIMARY KEY
);

INSERT INTO tmp_intro_seq (n)
VALUES (1),
       (2),
       (3),
       (4),
       (5),
       (6),
       (7),
       (8),
       (9),
       (10),
       (11),
       (12),
       (13),
       (14),
       (15),
       (16),
       (17),
       (18),
       (19),
       (20),
       (21),
       (22),
       (23),
       (24),
       (25),
       (26),
       (27),
       (28),
       (29),
       (30);

SET
@male_count = (SELECT COUNT(*) FROM members WHERE gender = 'MALE' AND activity_status = 'ACTIVE');
SET
@female_count = (SELECT COUNT(*) FROM members WHERE gender = 'FEMALE' AND activity_status = 'ACTIVE');

-- ------------------------------------------------------------
-- 2. 남성 → 여성 소개 생성
-- ------------------------------------------------------------
-- 남성 id: 홀수(1,3,5,...), 여성 id: 짝수(2,4,6,...)
INSERT INTO member_introductions (created_at, updated_at, member_id, introduced_member_id, type)
SELECT NOW(6),
       NOW(6),
       m.id,
       2 * ((((m.id - 1) DIV 2) + s.n * 7) % @female_count + 1),
    ELT(((m.id + s.n) % 9) + 1,
        'DIAMOND_GRADE', 'SAME_HOBBY', 'SAME_RELIGION', 'SAME_CITY', 'RECENTLY_JOINED',
        'TODAY_CARD', 'SOULMATE', 'SAME_ANSWER', 'IDEAL')
FROM members m
    CROSS JOIN tmp_intro_seq s
WHERE m.gender = 'MALE'
  AND m.activity_status = 'ACTIVE'
  AND s.n <= @INTROS_PER_MEMBER
ON DUPLICATE KEY
UPDATE updated_at = NOW(6);

COMMIT;

-- ------------------------------------------------------------
-- 3. 여성 → 남성 소개 생성
-- ------------------------------------------------------------
INSERT INTO member_introductions (created_at, updated_at, member_id, introduced_member_id, type)
SELECT NOW(6),
       NOW(6),
       m.id,
       2 * ((((m.id DIV 2) - 1) + s.n * 7) % @male_count) + 1,
    ELT(((m.id + s.n + 3) % 9) + 1,
        'DIAMOND_GRADE', 'SAME_HOBBY', 'SAME_RELIGION', 'SAME_CITY', 'RECENTLY_JOINED',
        'TODAY_CARD', 'SOULMATE', 'SAME_ANSWER', 'IDEAL')
FROM members m
    CROSS JOIN tmp_intro_seq s
WHERE m.gender = 'FEMALE'
  AND m.activity_status = 'ACTIVE'
  AND s.n <= @INTROS_PER_MEMBER
ON DUPLICATE KEY
UPDATE updated_at = NOW(6);

COMMIT;

-- 임시 테이블 정리
DROP TABLE IF EXISTS tmp_intro_seq;

-- 결과 확인
SELECT CONCAT('02_introductions 완료: ', COUNT(*), '건') AS status
FROM member_introductions;
