package kr.co.deepple.match.application.port.in;

import java.util.List;

public interface FindMatchHistoriesUseCase {
  List<MatchHistory> findMatchHistoriesOf(long memberId);

  List<MatchHistory> findMatchHistoriesBetween(long memberId, long anotherMemberId);
}
