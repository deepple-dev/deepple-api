-- ============================================================
-- 최종 정리 및 데이터 검증
-- ============================================================

-- 성능 설정 복원
SET
FOREIGN_KEY_CHECKS = 1;
SET
UNIQUE_CHECKS = 1;
SET
autocommit = 1;

-- 임시 테이블 삭제
DROP TABLE IF EXISTS tmp_numbers;

-- 데이터 요약
SELECT 'members' AS `table`, COUNT(*) AS count
FROM members
UNION ALL
SELECT 'member_introductions', COUNT(*)
FROM member_introductions
UNION ALL
SELECT 'likes', COUNT(*)
FROM likes
UNION ALL
SELECT 'matches', COUNT(*)
FROM matches
UNION ALL
SELECT 'notifications', COUNT(*)
FROM notifications;
