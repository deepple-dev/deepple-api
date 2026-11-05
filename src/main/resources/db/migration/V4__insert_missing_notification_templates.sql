INSERT INTO notification_templates (type, title_template, body_template, is_active)
VALUES ('REPORT', '신고 누적 경고', '다른 회원들의 신고가 누적되었습니다. 운영 정책을 위반하는 활동이 계속되면 서비스 이용이 제한될 수 있습니다.', 1),
       ('HEART_GRANTED', '하트 지급', '운영자로부터 {amount} 하트가 지급되었습니다.', 1);
