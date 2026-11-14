package kr.co.deepple.match.application.port.in;

import java.util.Optional;

public interface FindMatchBetweenUseCase {
  Optional<MatchSummary> findActiveMatchBetween(long memberId, long anotherMemberId);

  boolean hasReceivedMatchRequest(long responderId, long requesterId);
}
