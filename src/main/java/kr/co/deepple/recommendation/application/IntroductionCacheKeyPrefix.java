package kr.co.deepple.recommendation.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntroductionCacheKeyPrefix {
  DIAMOND("diamond:"),
  SAME_HOBBY("hobby:"),
  SAME_RELIGION("religion:"),
  SAME_CITY("city:"),
  RECENTLY_JOINED("recentlyJoined:"),
  TODAY_CARD("todayCard:"),
  IDEAL("ideal:");

  private final String prefix;
}
