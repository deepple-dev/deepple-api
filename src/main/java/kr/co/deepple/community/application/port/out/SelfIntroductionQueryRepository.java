package kr.co.deepple.community.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.community.application.dto.RawSelfIntroductionView;

public interface SelfIntroductionQueryRepository {
  List<RawSelfIntroductionView> findSelfIntroductions(
      long memberId, Long lastId, Set<Long> blockedIds, int limit);

  List<RawSelfIntroductionView> findMySelfIntroductions(long memberId, Long lastId, int limit);

  Optional<RawSelfIntroductionView> findSelfIntroduction(long id);

  Optional<String> findProfileExchangeStatus(long memberId, long otherMemberId);
}
