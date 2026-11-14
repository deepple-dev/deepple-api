package kr.co.deepple.recommendation.application.port.out;

import java.util.Collection;
import java.util.Set;

public interface IntroductionQueryRepository {
  Set<Long> findAllIntroducedMemberId(long memberId);

  Set<Long> findIntroducedMemberIds(long memberId, Collection<Long> memberIds);
}
