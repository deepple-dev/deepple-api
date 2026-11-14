package kr.co.deepple.recommendation.application.port.out;

import java.util.Date;
import java.util.Set;

public interface IntroductionCacheRepository {
  void saveIntroductionMemberIds(String key, Set<Long> introductionMemberIds, Date expireAt);

  Set<Long> findIntroductionMemberIds(String key);
}
