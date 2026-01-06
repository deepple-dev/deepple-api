-- ============================================================
-- 04. 매칭 데이터 생성
-- ============================================================
-- 좋아요 보낸 회원에게만 매칭 요청 가능

SET
@MATCH_RATIO = 30; -- 좋아요 중 매칭 비율 (%)

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE matches;
SET
FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 매칭 생성
-- ------------------------------------------------------------
INSERT INTO matches (created_at, updated_at,
                     requester_id, responder_id,
                     request_message, response_message,
                     status, requester_contact_type, responder_contact_type,
                     read_by_responder_at)
SELECT NOW(6),
       NOW(6),
       l.sender_id,
       l.receiver_id,
       '안녕하세요! 프로필 보고 연락드려요 :)',
       CASE WHEN (l.id % 100) < 50 THEN '반갑습니다! 저도 관심있어요 :)' ELSE NULL END,
       CASE
           WHEN (l.id % 100) < 30 THEN 'WAITING'
           WHEN (l.id % 100) < 80 THEN 'MATCHED'
           WHEN (l.id % 100) < 90 THEN 'REJECTED'
           WHEN (l.id % 100) < 95 THEN 'REJECT_CHECKED'
           ELSE 'EXPIRED'
           END,
       IF(l.id % 3 = 0, 'PHONE_NUMBER', 'KAKAO'),
       IF(l.id % 4 = 0, 'PHONE_NUMBER', 'KAKAO'),
       IF((l.id % 100) < 60, NOW(6), NULL)
FROM likes l
WHERE (l.id % 100) < @MATCH_RATIO;

COMMIT;

-- 결과 확인
SELECT CONCAT('04_matches 완료: ', COUNT(*), '건') AS status
FROM matches;
