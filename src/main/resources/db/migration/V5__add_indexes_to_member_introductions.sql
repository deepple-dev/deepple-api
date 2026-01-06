-- memberId, introducedMemberId 복합 조회 성능 향상을 위한 복합 인덱스
-- memberId 단독 조회에도 사용 가능
CREATE INDEX idx_member_introductions_member_introduced
    ON member_introductions (member_id, introduced_member_id);
