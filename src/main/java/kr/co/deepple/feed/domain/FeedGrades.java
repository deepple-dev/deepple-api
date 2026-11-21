package kr.co.deepple.feed.domain;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedGrades {

  public static final Set<String> ALL = Set.of("DIAMOND", "GOLD", "SILVER");
}
