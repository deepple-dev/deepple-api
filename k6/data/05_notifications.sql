-- ============================================================
-- 05. 알림 데이터 생성
-- ============================================================

-- 기존 데이터 삭제
SET
FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE notification_preferences;
TRUNCATE TABLE member_notification_preferences;
TRUNCATE TABLE device_registrations;
TRUNCATE TABLE notification_templates;
SET
FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. 알림 템플릿 생성
-- ------------------------------------------------------------
INSERT INTO notification_templates (type, title_template, body_template, is_active)
VALUES
-- 매치
('MATCH_REQUEST', '매치 요청', '{senderName}님에게 메시지가 도착하였습니다.', true),
('MATCH_ACCEPT', '매치 수락', '{senderName}님과 매칭되었습니다. 축하합니다!', true),
('MATCH_REJECT', '매치 거절', '{senderName}님이 데이트 신청을 거절하셨습니다.', true),
-- 프로필 교환
('PROFILE_EXCHANGE_REQUEST', '프로필 교환 요청', '{senderName}님이 프로필 교환을 요청하였습니다.', true),
('PROFILE_EXCHANGE_ACCEPT', '프로필 교환 수락', '{senderName}님이 프로필 교환을 수락하셨습니다!', true),
('PROFILE_EXCHANGE_REJECT', '프로필 교환 거절', '{senderName}님이 프로필 교환을 거절하셨습니다.', true),
-- 좋아요
('LIKE', '좋아요', '{senderName}님이 회원님의 프로필을 좋아합니다.', true),
-- 프로필 이미지 변경 요청
('PROFILE_IMAGE_CHANGE_REQUEST', '프로필 이미지 변경 요청', '나를 더 잘 표현할 수 있는 프로필 이미지로 설정해보세요!', true),
-- 인터뷰 작성 요청
('INTERVIEW_WRITE_REQUEST', '인터뷰 작성 요청', '아직 인터뷰를 작성하지 않으셨어요! 인터뷰 작성하고 무료 하트 받아 가세요.', true),
-- 경고 알림
('INAPPROPRIATE_PROFILE', '부적절한 프로필 경고', '등록하신 프로필 정보를 변경해 주세요. 개인 정보 혹은 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', true),
('INAPPROPRIATE_PROFILE_IMAGE', '부적절한 프로필 사진 경고', '등록하신 프로필 사진을 변경해 주세요. 부적절한 사진 등록 시 서비스 이용이 제한될 수 있습니다.', true),
('INAPPROPRIATE_INTERVIEW', '부적절한 인터뷰 경고', '등록하신 인터뷰를 변경해 주세요. 개인 정보 혹은 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', true),
('INAPPROPRIATE_SELF_INTRODUCTION', '부적절한 셀프소개 게시글 경고',
 '작성하신 게시글에 부적절한 내용이 포함되어 있습니다. 부적절한 내용 등록 시 서비스 이용이 제한될 수 있습니다.', true),
-- 장기 미로그인
('INACTIVITY_REMINDER', '다시 방문해보세요', '장기간 접속이 없으시네요! 다시 연애를 시작해 볼까요?', true),
-- 심사
('SCREENING_APPROVED', '심사 승인', '프로필 심사가 완료되었어요! 안전하고 진정성 있는 만남을 시작해 보세요.', true),
('SCREENING_REJECTED', '심사 반려', '심사가 반려되었습니다. 등록하신 프로필을 변경해주세요. (반려 사유: {rejectionReason})', true);

COMMIT;

-- ------------------------------------------------------------
-- 2. 알림 설정 생성
-- ------------------------------------------------------------
INSERT INTO notification_preferences (created_at, updated_at, deleted_at, member_id, is_enabled_globally)
SELECT NOW(6), NOW(6), NULL, id, TRUE
FROM members
WHERE activity_status = 'ACTIVE';

COMMIT;

-- ------------------------------------------------------------
-- 3. 회원별 알림 타입 설정 (모든 타입 활성화)
-- ------------------------------------------------------------
INSERT INTO member_notification_preferences (member_id, notification_type, is_enabled)
SELECT np.id, nt.type, TRUE
FROM notification_preferences np
         CROSS JOIN (SELECT 'MATCH_REQUEST' AS type
                     UNION ALL
                     SELECT 'MATCH_ACCEPT'
                     UNION ALL
                     SELECT 'MATCH_REJECT'
                     UNION ALL
                     SELECT 'PROFILE_EXCHANGE_REQUEST'
                     UNION ALL
                     SELECT 'PROFILE_EXCHANGE_ACCEPT'
                     UNION ALL
                     SELECT 'PROFILE_EXCHANGE_REJECT'
                     UNION ALL
                     SELECT 'LIKE'
                     UNION ALL
                     SELECT 'PROFILE_IMAGE_CHANGE_REQUEST'
                     UNION ALL
                     SELECT 'INTERVIEW_WRITE_REQUEST'
                     UNION ALL
                     SELECT 'INAPPROPRIATE_PROFILE'
                     UNION ALL
                     SELECT 'INAPPROPRIATE_PROFILE_IMAGE'
                     UNION ALL
                     SELECT 'INAPPROPRIATE_INTERVIEW'
                     UNION ALL
                     SELECT 'INAPPROPRIATE_SELF_INTRODUCTION'
                     UNION ALL
                     SELECT 'INACTIVITY_REMINDER'
                     UNION ALL
                     SELECT 'SCREENING_APPROVED'
                     UNION ALL
                     SELECT 'SCREENING_REJECTED') nt;

COMMIT;

-- ------------------------------------------------------------
-- 4. 디바이스 등록 (회원당 1개)
-- ------------------------------------------------------------
INSERT INTO device_registrations (member_id, device_id, registration_token, is_active)
SELECT id,
       CONCAT('device_', id),
       CONCAT('fcm_token_', id),
       TRUE
FROM members
WHERE activity_status = 'ACTIVE';

COMMIT;

-- ------------------------------------------------------------
-- 5. 알림 생성 (회원당 30개, 총 3000만 건)
-- ------------------------------------------------------------
INSERT INTO notifications (created_at, updated_at, deleted_at,
                           sender_type, sender_id, receiver_id,
                           type, title, body, status, read_at)
SELECT DATE_SUB(NOW(6), INTERVAL ((m.id + seq.n) % 30) DAY),
       NOW(6),
       NULL,
       'MEMBER',
       m.id,
       IF(m.id % 2 = 1, m.id + 1, m.id - 1),
       ELT(((m.id + seq.n) % 16) + 1,
           'MATCH_REQUEST', 'MATCH_ACCEPT', 'MATCH_REJECT',
           'PROFILE_EXCHANGE_REQUEST', 'PROFILE_EXCHANGE_ACCEPT', 'PROFILE_EXCHANGE_REJECT',
           'LIKE',
           'PROFILE_IMAGE_CHANGE_REQUEST', 'INTERVIEW_WRITE_REQUEST',
           'INAPPROPRIATE_PROFILE', 'INAPPROPRIATE_PROFILE_IMAGE',
           'INAPPROPRIATE_INTERVIEW', 'INAPPROPRIATE_SELF_INTRODUCTION',
           'INACTIVITY_REMINDER', 'SCREENING_APPROVED', 'SCREENING_REJECTED'),
       ELT(((m.id + seq.n) % 16) + 1,
           '매치 요청', '매치 수락', '매치 거절',
           '프로필 교환 요청', '프로필 교환 수락', '프로필 교환 거절',
           '좋아요',
           '프로필 이미지 변경 요청', '인터뷰 작성 요청',
           '부적절한 프로필 경고', '부적절한 프로필 사진 경고',
           '부적절한 인터뷰 경고', '부적절한 셀프소개 게시글 경고',
           '다시 방문해보세요', '심사 승인', '심사 반려'),
       '테스트 알림 본문입니다.',
       'SENT',
       IF(((m.id + seq.n) % 100) < 30, DATE_SUB(NOW(6), INTERVAL ((m.id + seq.n) % 7) DAY), NULL)
FROM members m
         CROSS JOIN (SELECT 1 AS n
                     UNION ALL
                     SELECT 2
                     UNION ALL
                     SELECT 3
                     UNION ALL
                     SELECT 4
                     UNION ALL
                     SELECT 5
                     UNION ALL
                     SELECT 6
                     UNION ALL
                     SELECT 7
                     UNION ALL
                     SELECT 8
                     UNION ALL
                     SELECT 9
                     UNION ALL
                     SELECT 10
                     UNION ALL
                     SELECT 11
                     UNION ALL
                     SELECT 12
                     UNION ALL
                     SELECT 13
                     UNION ALL
                     SELECT 14
                     UNION ALL
                     SELECT 15
                     UNION ALL
                     SELECT 16
                     UNION ALL
                     SELECT 17
                     UNION ALL
                     SELECT 18
                     UNION ALL
                     SELECT 19
                     UNION ALL
                     SELECT 20
                     UNION ALL
                     SELECT 21
                     UNION ALL
                     SELECT 22
                     UNION ALL
                     SELECT 23
                     UNION ALL
                     SELECT 24
                     UNION ALL
                     SELECT 25
                     UNION ALL
                     SELECT 26
                     UNION ALL
                     SELECT 27
                     UNION ALL
                     SELECT 28
                     UNION ALL
                     SELECT 29
                     UNION ALL
                     SELECT 30) seq;

COMMIT;

-- 결과 확인
SELECT CONCAT('05_notifications 완료: ', COUNT(*), '건') AS status
FROM notifications;
