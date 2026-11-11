package kr.co.deepple.member.query.introduction.application;

import lombok.Getter;

@Getter
public enum IntroductionCacheKeyPrefix {
    DIAMOND("diamond:"),
    SAME_HOBBY("hobby:"),
    SAME_RELIGION("religion:"),
    SAME_CITY("city:"),
    RECENTLY_JOINED("recentlyJoined:"),
    TODAY_CARD("todayCard:"),
    IDEAL("ideal:");

    private final String prefix;

    IntroductionCacheKeyPrefix(String prefix) {
        this.prefix = prefix;
    }
}
