package kr.co.deepple.match.application.port.out;

import java.util.Optional;
import java.util.Set;
import kr.co.deepple.match.domain.Match;

public interface MatchRepository {
  void save(Match match);

  boolean existsActiveMatchBetween(long memberId, long anotherMemberId);

  Optional<Match> findActiveMatchBetween(long memberId, long anotherMemberId);

  boolean existsReceivedMatchRequest(long responderId, long requesterId);

  Optional<Match> findById(long id);

  Optional<Match> findByIdAndRequesterId(long id, long requesterId);

  Optional<Match> findByIdAndResponderId(long id, long responderId);

  Set<Long> findRequesterIdsByResponderId(long responderId);

  Set<Long> findResponderIdsByRequesterId(long requesterId);
}
