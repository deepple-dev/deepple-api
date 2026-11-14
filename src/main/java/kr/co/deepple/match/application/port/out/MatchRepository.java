package kr.co.deepple.match.application.port.out;

import java.util.Optional;
import kr.co.deepple.match.domain.Match;

public interface MatchRepository {
  void save(Match match);

  boolean existsActiveMatchBetween(long memberId, long anotherMemberId);

  Optional<Match> findById(long id);

  Optional<Match> findByIdAndRequesterId(long id, long requesterId);

  Optional<Match> findByIdAndResponderId(long id, long responderId);
}
