-- ============================================================
-- 01. 회원 데이터 생성
-- ============================================================

SET
@TARGET_COUNT = 1000000; -- 생성할 회원 수

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE member_hobbies;
TRUNCATE TABLE member_ideals;
TRUNCATE TABLE profile_images;
TRUNCATE TABLE members;
SET
FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 숫자 시퀀스 테이블 생성 (1 ~ 1,000,000)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tmp_numbers;
CREATE TABLE tmp_numbers
(
    n INT PRIMARY KEY
);

DROP TABLE IF EXISTS tmp_seq;
CREATE TABLE tmp_seq
(
    n INT PRIMARY KEY
);

INSERT INTO tmp_seq (n)
WITH RECURSIVE seq AS (SELECT 1 AS n
                       UNION ALL
                       SELECT n + 1
                       FROM seq
                       WHERE n < 1000)
SELECT n
FROM seq;

INSERT INTO tmp_numbers (n)
SELECT a.n + (b.n - 1) * 1000
FROM tmp_seq a
         CROSS JOIN tmp_seq b
WHERE a.n + (b.n - 1) * 1000 <= @TARGET_COUNT;

DROP TABLE tmp_seq;

-- ------------------------------------------------------------
-- 2. 회원 생성
-- ------------------------------------------------------------
INSERT INTO members (created_at, updated_at, deleted_at,
                     phone_number, kakao_id, nickname,
                     gender, year_of_birth, height,
                     city, district, job, religion,
                     smoking_status, drinking_status, highest_education, mbti,
                     activity_status, grade, primary_contact_type,
                     is_profile_public, is_vip, is_dating_exam_submitted,
                     mission_heart_balance, purchase_heart_balance)
SELECT NOW(6),
       NOW(6),
       NULL,
       CONCAT('010', LPAD(n.n, 8, '0')),
       CONCAT('kakao_', n.n),
       CONCAT('User', n.n),
       IF(n.n % 2 = 1, 'MALE', 'FEMALE'),
       1980 + (n.n % 23),
       IF(n.n % 2 = 1, 165 + (n.n % 21), 155 + (n.n % 16)),
       ELT((n.n % 17) + 1,
           'SEOUL', 'INCHEON', 'BUSAN', 'DAEJEON', 'DAEGU', 'GWANGJU', 'ULSAN', 'JEJU', 'SEJONG',
           'GANGWON', 'GYEONGGI', 'GYEONGSANGNAM', 'GYEONGSANGBUK', 'CHUNGCHEONGNAM', 'CHUNGCHEONGBUK',
           'JEOLLANAM', 'JEOLLABUK'),
       'GANGNAM_GU',
       ELT((n.n % 20) + 1,
           'RESEARCH_AND_ENGINEERING', 'SELF_EMPLOYMENT', 'SALES', 'MANAGEMENT_AND_PLANNING',
           'STUDYING_FOR_FUTURE', 'JOB_SEARCHING', 'EDUCATION', 'ARTS_AND_SPORTS', 'FOOD_SERVICE',
           'MEDICAL_AND_HEALTH', 'MECHANICAL_AND_CONSTRUCTION', 'DESIGN', 'MARKETING_AND_ADVERTISING',
           'TRADE_AND_DISTRIBUTION', 'MEDIA_AND_ENTERTAINMENT', 'LEGAL_AND_PUBLIC',
           'PRODUCTION_AND_MANUFACTURING', 'CUSTOMER_SERVICE', 'TRAVEL_AND_TRANSPORT', 'OTHERS'),
       ELT((n.n % 5) + 1, 'NONE', 'CHRISTIAN', 'CATHOLIC', 'BUDDHIST', 'OTHER'),
       ELT((n.n % 5) + 1, 'NONE', 'QUIT', 'OCCASIONAL', 'DAILY', 'VAPE'),
       ELT((n.n % 5) + 1, 'NONE', 'QUIT', 'SOCIAL', 'OCCASIONAL', 'FREQUENT'),
       ELT((n.n % 9) + 1,
           'HIGH_SCHOOL', 'ASSOCIATE', 'BACHELORS_LOCAL', 'BACHELORS_SEOUL', 'BACHELORS_OVERSEAS',
           'LAW_SCHOOL', 'MASTERS', 'DOCTORATE', 'OTHER'),
       ELT((n.n % 16) + 1,
           'ESFP', 'ESFJ', 'ESTP', 'ESTJ', 'ENFP', 'ENFJ', 'ENTP', 'ENTJ',
           'ISFP', 'ISFJ', 'ISTP', 'ISTJ', 'INFP', 'INFJ', 'INTP', 'INTJ'),
       'ACTIVE',
       CASE
           WHEN n.n % 100 < 70 THEN 'SILVER'
           WHEN n.n % 100 < 95 THEN 'GOLD'
           ELSE 'DIAMOND'
           END,
       'KAKAO',
       TRUE,
       FALSE,
       n.n % 2,
    (n.n % 500),
    100 + (n.n % 1000)
FROM tmp_numbers n
WHERE n.n <= @TARGET_COUNT;

COMMIT;

-- ------------------------------------------------------------
-- 3. 회원 취미 생성 (회원당 3개)
-- ------------------------------------------------------------
INSERT INTO member_hobbies (member_id, name)
SELECT m.id,
       ELT((m.id % 29) + 1,
           'TRAVEL', 'PERFORMANCE_AND_EXHIBITION', 'WEBTOON_AND_COMICS', 'DRAMA_AND_ENTERTAINMENT',
           'PC_AND_MOBILE_GAMES', 'ANIMATION', 'GOLF', 'THEATER_AND_MOVIES', 'WRITING', 'BOARD_GAMES',
           'PHOTOGRAPHY', 'SINGING', 'BADMINTON_AND_TENNIS', 'DANCE', 'DRIVING', 'HIKING_AND_CLIMBING',
           'WALKING', 'FOOD_HUNT', 'SHOPPING', 'SKI_AND_SNOWBOARD', 'PLAYING_INSTRUMENTS', 'WINE',
           'WORKOUT', 'YOGA_AND_PILATES', 'COOKING', 'INTERIOR_DESIGN', 'CYCLING', 'CAMPING', 'OTHERS')
FROM members m;

INSERT INTO member_hobbies (member_id, name)
SELECT m.id,
       ELT(((m.id + 7) % 29) + 1,
    'TRAVEL', 'PERFORMANCE_AND_EXHIBITION', 'WEBTOON_AND_COMICS', 'DRAMA_AND_ENTERTAINMENT',
    'PC_AND_MOBILE_GAMES', 'ANIMATION', 'GOLF', 'THEATER_AND_MOVIES', 'WRITING', 'BOARD_GAMES',
    'PHOTOGRAPHY', 'SINGING', 'BADMINTON_AND_TENNIS', 'DANCE', 'DRIVING', 'HIKING_AND_CLIMBING',
    'WALKING', 'FOOD_HUNT', 'SHOPPING', 'SKI_AND_SNOWBOARD', 'PLAYING_INSTRUMENTS', 'WINE',
    'WORKOUT', 'YOGA_AND_PILATES', 'COOKING', 'INTERIOR_DESIGN', 'CYCLING', 'CAMPING', 'OTHERS')
FROM members m;

INSERT INTO member_hobbies (member_id, name)
SELECT m.id,
       ELT(((m.id + 13) % 29) + 1,
    'TRAVEL', 'PERFORMANCE_AND_EXHIBITION', 'WEBTOON_AND_COMICS', 'DRAMA_AND_ENTERTAINMENT',
    'PC_AND_MOBILE_GAMES', 'ANIMATION', 'GOLF', 'THEATER_AND_MOVIES', 'WRITING', 'BOARD_GAMES',
    'PHOTOGRAPHY', 'SINGING', 'BADMINTON_AND_TENNIS', 'DANCE', 'DRIVING', 'HIKING_AND_CLIMBING',
    'WALKING', 'FOOD_HUNT', 'SHOPPING', 'SKI_AND_SNOWBOARD', 'PLAYING_INSTRUMENTS', 'WINE',
    'WORKOUT', 'YOGA_AND_PILATES', 'COOKING', 'INTERIOR_DESIGN', 'CYCLING', 'CAMPING', 'OTHERS')
FROM members m;

COMMIT;

-- ------------------------------------------------------------
-- 4. 회원 이상형 생성
-- ------------------------------------------------------------
INSERT INTO member_ideals (created_at, updated_at, member_id, min_age, max_age, religion, smoking_status,
                           drinking_status)
SELECT NOW(6),
       NOW(6),
       m.id,
       20,
       40,
       ELT((m.id % 5) + 1, 'NONE', 'CHRISTIAN', 'CATHOLIC', 'BUDDHIST', 'OTHER'),
       ELT((m.id % 5) + 1, 'NONE', 'QUIT', 'OCCASIONAL', 'DAILY', 'VAPE'),
       ELT((m.id % 5) + 1, 'NONE', 'QUIT', 'SOCIAL', 'OCCASIONAL', 'FREQUENT')
FROM members m;

COMMIT;

-- ------------------------------------------------------------
-- 5. 프로필 이미지 생성 (회원당 2장)
-- ------------------------------------------------------------
INSERT INTO profile_images (created_at, updated_at, member_id, url, is_primary, profile_order)
SELECT NOW(6), NOW(6), m.id, CONCAT('https://example.com/profile/', m.id, '_1.jpg'), TRUE, 1
FROM members m;

INSERT INTO profile_images (created_at, updated_at, member_id, url, is_primary, profile_order)
SELECT NOW(6), NOW(6), m.id, CONCAT('https://example.com/profile/', m.id, '_2.jpg'), FALSE, 2
FROM members m;

COMMIT;

-- 임시 테이블 정리
DROP TABLE IF EXISTS tmp_numbers;

-- 결과 확인
SELECT CONCAT('01_members 완료: ', COUNT(*), '명') AS status
FROM members;
