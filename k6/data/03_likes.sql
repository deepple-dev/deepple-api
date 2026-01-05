-- ============================================================
-- 좋아요 데이터 생성
-- 소개받은 회원에게만 좋아요 가능하므로 소개 관계 기반으로 생성
-- ============================================================

-- 소개 관계 중 좋아요 비율
SET
@LIKE_RATIO = 30;  -- 30%

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE likes;
SET
FOREIGN_KEY_CHECKS = 1;

-- 좋아요 생성 (소개받은 회원에게)
INSERT INTO likes (created_at, updated_at, sender_id, receiver_id, level)
SELECT NOW(6),
       NOW(6),
       mi.member_id,
       mi.introduced_member_id,
       IF((mi.member_id + mi.introduced_member_id) % 10 < 7, 'INTERESTED', 'HIGHLY_INTERESTED')
FROM member_introductions mi
WHERE (mi.member_id % 100) < @LIKE_RATIO ON DUPLICATE KEY
UPDATE updated_at = NOW(6);

COMMIT;

-- 상호 좋아요 생성 (20%)
SET
@MUTUAL_RATIO = 20;

INSERT INTO likes (created_at, updated_at, sender_id, receiver_id, level)
SELECT NOW(6),
       NOW(6),
       l.receiver_id,
       l.sender_id,
       IF((l.sender_id + l.receiver_id) % 2 = 0, 'INTERESTED', 'HIGHLY_INTERESTED')
FROM likes l
         JOIN member_introductions mi
              ON mi.member_id = l.receiver_id
                  AND mi.introduced_member_id = l.sender_id
WHERE (l.id % 100) < @MUTUAL_RATIO ON DUPLICATE KEY
UPDATE updated_at = NOW(6);

COMMIT;

SELECT CONCAT('03_likes 완료: ', COUNT(*), '건') AS status
FROM likes;
