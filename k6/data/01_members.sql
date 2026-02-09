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
-- 닉네임: 남성 m_1, m_2, ... / 여성 w_1, w_2, ...
-- 나이: 28~36세 (1989~1997년생)
-- 키: 150~199 랜덤
-- 연락처: 010-0000-0000 ~ 010-9999-9999 중복없이
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
       IF(n.n % 2 = 1, CONCAT('m_', CEILING(n.n / 2)), CONCAT('w_', n.n DIV 2)),
       IF(n.n % 2 = 1, 'MALE', 'FEMALE'),
       1989 + FLOOR(RAND() * 9),
       150 + FLOOR(RAND() * 50),
       ELT(FLOOR(1 + RAND() * 17),
           'SEOUL', 'INCHEON', 'BUSAN', 'DAEJEON', 'DAEGU', 'GWANGJU', 'ULSAN', 'JEJU', 'SEJONG',
           'GANGWON', 'GYEONGGI', 'GYEONGSANGNAM', 'GYEONGSANGBUK', 'CHUNGCHEONGNAM', 'CHUNGCHEONGBUK',
           'JEOLLANAM', 'JEOLLABUK'),
       'GANGNAM_GU',
       ELT(FLOOR(1 + RAND() * 20),
           'RESEARCH_AND_ENGINEERING', 'SELF_EMPLOYMENT', 'SALES', 'MANAGEMENT_AND_PLANNING',
           'STUDYING_FOR_FUTURE', 'JOB_SEARCHING', 'EDUCATION', 'ARTS_AND_SPORTS', 'FOOD_SERVICE',
           'MEDICAL_AND_HEALTH', 'MECHANICAL_AND_CONSTRUCTION', 'DESIGN', 'MARKETING_AND_ADVERTISING',
           'TRADE_AND_DISTRIBUTION', 'MEDIA_AND_ENTERTAINMENT', 'LEGAL_AND_PUBLIC',
           'PRODUCTION_AND_MANUFACTURING', 'CUSTOMER_SERVICE', 'TRAVEL_AND_TRANSPORT', 'OTHERS'),
       ELT(FLOOR(1 + RAND() * 5), 'NONE', 'CHRISTIAN', 'CATHOLIC', 'BUDDHIST', 'OTHER'),
       ELT(FLOOR(1 + RAND() * 5), 'NONE', 'QUIT', 'OCCASIONAL', 'DAILY', 'VAPE'),
       ELT(FLOOR(1 + RAND() * 5), 'NONE', 'QUIT', 'SOCIAL', 'OCCASIONAL', 'FREQUENT'),
       ELT(FLOOR(1 + RAND() * 9),
           'HIGH_SCHOOL', 'ASSOCIATE', 'BACHELORS_LOCAL', 'BACHELORS_SEOUL', 'BACHELORS_OVERSEAS',
           'LAW_SCHOOL', 'MASTERS', 'DOCTORATE', 'OTHER'),
       ELT(FLOOR(1 + RAND() * 16),
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
-- 3. 회원 취미 생성 (회원당 1개)
-- ------------------------------------------------------------
INSERT INTO member_hobbies (member_id, name)
SELECT m.id,
       ELT(FLOOR(1 + RAND() * 29),
           'TRAVEL', 'PERFORMANCE_AND_EXHIBITION', 'WEBTOON_AND_COMICS', 'DRAMA_AND_ENTERTAINMENT',
           'PC_AND_MOBILE_GAMES', 'ANIMATION', 'GOLF', 'THEATER_AND_MOVIES', 'WRITING', 'BOARD_GAMES',
           'PHOTOGRAPHY', 'SINGING', 'BADMINTON_AND_TENNIS', 'DANCE', 'DRIVING', 'HIKING_AND_CLIMBING',
           'WALKING', 'FOOD_HUNT', 'SHOPPING', 'SKI_AND_SNOWBOARD', 'PLAYING_INSTRUMENTS', 'WINE',
           'WORKOUT', 'YOGA_AND_PILATES', 'COOKING', 'INTERIOR_DESIGN', 'CYCLING', 'CAMPING', 'OTHERS')
FROM members m;

COMMIT;

-- ------------------------------------------------------------
-- 4. 회원 이상형 생성 (초기값 - 모두 null)
-- ------------------------------------------------------------
INSERT INTO member_ideals (created_at, updated_at, member_id, min_age, max_age, religion, smoking_status,
                           drinking_status)
SELECT NOW(6),
       NOW(6),
       m.id,
       NULL,
       NULL,
       NULL,
       NULL,
       NULL
FROM members m;

COMMIT;

-- ------------------------------------------------------------
-- 5. 프로필 이미지 생성 (회원당 1장, 성별별 이미지)
-- ------------------------------------------------------------
-- 남성: https://deepple-prod-storage.s3.ap-northeast-2.amazonaws.com/1048561/466097308_896662852172522_47988989484893266_n.jpg
-- 여성: https://deepple-prod-storage.s3.ap-northeast-2.amazonaws.com/1048561/436140859_1375057459846905_6604778979812319034_n.jpg
INSERT INTO profile_images (created_at, updated_at, member_id, url, is_primary, profile_order)
SELECT NOW(6),
       NOW(6),
       m.id,
       IF(m.gender = 'MALE',
          'https://deepple-prod-storage.s3.ap-northeast-2.amazonaws.com/1048561/466097308_896662852172522_47988989484893266_n.jpg',
          'https://deepple-prod-storage.s3.ap-northeast-2.amazonaws.com/1048561/436140859_1375057459846905_6604778979812319034_n.jpg'),
       TRUE,
       1
FROM members m;

COMMIT;

-- 임시 테이블 정리
DROP TABLE IF EXISTS tmp_numbers;

-- 결과 확인
SELECT CONCAT('01_members 완료: ', COUNT(*), '명') AS status
FROM members;
