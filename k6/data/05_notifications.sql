-- ============================================================
-- 05. 알림 데이터 생성
-- ============================================================

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE notification_preferences;
SET
FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 알림 설정 생성
-- ------------------------------------------------------------
INSERT INTO notification_preferences (created_at, updated_at, deleted_at, member_id, is_enabled_globally)
SELECT NOW(6), NOW(6), NULL, id, TRUE
FROM members
WHERE activity_status = 'ACTIVE';

COMMIT;

-- ------------------------------------------------------------
-- 2. 좋아요 알림 생성
-- ------------------------------------------------------------
INSERT INTO notifications (created_at, updated_at, deleted_at,
                           sender_type, sender_id, receiver_id,
                           type, title, body, status, read_at)
SELECT DATE_SUB(NOW(6), INTERVAL (l.id % 30) DAY),
       NOW(6),
       NULL,
       'MEMBER',
       l.sender_id,
       l.receiver_id,
       'LIKE',
       '새로운 좋아요',
       '회원님을 좋아하는 사람이 있어요!',
       'SENT',
       IF((l.id % 100) < 30, DATE_SUB(NOW(6), INTERVAL (l.id % 7) DAY), NULL)
FROM likes l;

COMMIT;

-- ------------------------------------------------------------
-- 3. 매칭 요청 알림 생성
-- ------------------------------------------------------------
INSERT INTO notifications (created_at, updated_at, deleted_at,
                           sender_type, sender_id, receiver_id,
                           type, title, body, status, read_at)
SELECT DATE_SUB(NOW(6), INTERVAL (m.id % 30) DAY),
       NOW(6),
       NULL,
       'MEMBER',
       m.requester_id,
       m.responder_id,
       'MATCH_REQUEST',
       '새로운 매칭 요청',
       '회원님께 매칭 요청이 도착했습니다.',
       'SENT',
       IF((m.id % 100) < 40, DATE_SUB(NOW(6), INTERVAL (m.id % 7) DAY), NULL)
FROM matches m;

COMMIT;

-- ------------------------------------------------------------
-- 4. 매칭 수락/거절 알림 생성
-- ------------------------------------------------------------
INSERT INTO notifications (created_at, updated_at, deleted_at,
                           sender_type, sender_id, receiver_id,
                           type, title, body, status, read_at)
SELECT DATE_SUB(NOW(6), INTERVAL (m.id % 20) DAY),
       NOW(6),
       NULL,
       'MEMBER',
       m.responder_id,
       m.requester_id,
       IF(m.status = 'MATCHED', 'MATCH_ACCEPT', 'MATCH_REJECT'),
       IF(m.status = 'MATCHED', '매칭이 수락되었습니다', '매칭이 거절되었습니다'),
       IF(m.status = 'MATCHED', '상대방이 매칭을 수락했습니다!', '아쉽지만 다음 기회에!'),
       'SENT',
       IF((m.id % 100) < 50, DATE_SUB(NOW(6), INTERVAL (m.id % 5) DAY), NULL)
FROM matches m
WHERE m.status IN ('MATCHED', 'REJECTED', 'REJECT_CHECKED');

COMMIT;

-- ------------------------------------------------------------
-- 5. 심사 승인 알림 생성
-- ------------------------------------------------------------
INSERT INTO notifications (created_at, updated_at, deleted_at,
                           sender_type, sender_id, receiver_id,
                           type, title, body, status, read_at)
SELECT DATE_SUB(NOW(6), INTERVAL 30 DAY),
       NOW(6),
       NULL,
       'SYSTEM',
       NULL,
       m.id,
       'SCREENING_APPROVED',
       '프로필 심사 승인',
       '프로필이 승인되었습니다.',
       'SENT',
       DATE_SUB(NOW(6), INTERVAL 29 DAY)
FROM members m
WHERE m.activity_status = 'ACTIVE';

COMMIT;

-- 결과 확인
SELECT CONCAT('05_notifications 완료: ', COUNT(*), '건') AS status
FROM notifications;
