package kr.co.deepple.match.application.port.out;

import java.util.List;
import java.util.Set;
import kr.co.deepple.match.application.dto.RawMatchView;

public interface MatchQueryRepository {
  List<RawMatchView> findSentMatches(long userId, Long lastMatchId, Set<Long> blockedIds);

  List<RawMatchView> findReceiveMatches(long userId, Long lastMatchId, Set<Long> blockedIds);
}
