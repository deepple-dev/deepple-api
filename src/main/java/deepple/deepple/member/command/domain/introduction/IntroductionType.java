package deepple.deepple.member.command.domain.introduction;

import lombok.Getter;

public enum IntroductionType {
    DIAMOND_GRADE("다이아 등급"),
    SAME_HOBBY("취미가 같아요"),
    SAME_RELIGION("종교가 같아요"),
    SAME_CITY("지역이 같아요"),
    RECENTLY_JOINED("최근 가입한 회원"),
    TODAY_CARD("오늘의 카드"),
    SOULMATE("소울 메이트"),
    SAME_ANSWER("연애 고사 답변 일치"),
    IDEAL("이상형"),
    PERSONALITY("유형별 이상형");

    @Getter
    private final String description;

    IntroductionType(String description) {
        this.description = description;
    }

    public boolean isFreeIntroduction() {
        return this == TODAY_CARD || this == SOULMATE;
    }
}
