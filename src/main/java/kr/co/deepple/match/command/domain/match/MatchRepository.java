package kr.co.deepple.match.command.domain.match;

import java.util.Optional;

public interface MatchRepository {
  void save(Match match);

  boolean existsActiveMatchBetween(long memberId, long anotherMemberId);

  Optional<Match> findById(long id);

  Optional<Match> findByIdAndRequesterId(long id, long requesterId);

  Optional<Match> findByIdAndResponderId(long id, long responderId);

  Optional<Match> findByRequesterIdAndResponderId(long requesterId, long responderId);
}
