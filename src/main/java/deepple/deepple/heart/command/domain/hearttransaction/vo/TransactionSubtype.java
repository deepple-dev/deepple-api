package deepple.deepple.heart.command.domain.hearttransaction.vo;

import lombok.Getter;

@Getter
public enum TransactionSubtype {
    DIAMOND_GRADE("다이아 등급"),
    SAME_HOBBY("취미가 같아요"),
    SAME_RELIGION("종교가 같아요"),
    SAME_CITY("지역이 같아요"),
    RECENTLY_JOINED("최근 가입한 회원"),
    TODAY_CARD("오늘의 카드"),
    SOULMATE("소울 메이트"),
    SAME_ANSWER("연애 고사 답변 일치"),
    IDEAL("이상형"),
    MATCH("매칭"),
    MATCH_ACCEPTED("매칭 수락"),
    PROFILE_EXCHANGE("프로필 교환");

    private final String description;

    TransactionSubtype(String description) {
        this.description = description;
    }

    public boolean isFreeEligible(TransactionType transactionType) {
        switch (transactionType) {
            case INTRODUCTION:
                return this == TODAY_CARD || this == SOULMATE;
            case MESSAGE:
                return this == SOULMATE;
            default:
                return false;
        }
    }
}
