package deepple.deepple.heart.command.domain.hearttransaction.vo;


import lombok.Getter;

public enum TransactionType {
    MISSION("미션 완료"),
    MESSAGE("메시지 보내기"),
    MESSAGE_ACCEPTED("메시지 수락"),
    DATING_EXAM("연애 모의고사 다시보기"),
    PROFILE_EXCHANGE("프로필 교환 신청"),
    INTRODUCTION("소개 받기"),
    PURCHASE("하트 구매"),
    ADMIN_GRANT("관리자 지급");

    @Getter
    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public boolean isUsingType() {
        return this == MESSAGE || this == DATING_EXAM || this == PROFILE_EXCHANGE || this == INTRODUCTION
            || this == MESSAGE_ACCEPTED;
    }

    public boolean isGainingType() {
        return this == MISSION || this == PURCHASE || this == ADMIN_GRANT;
    }
}
