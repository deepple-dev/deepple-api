package kr.co.deepple.community.application.port.in;

import java.util.List;
import java.util.Optional;

public interface FindProfileExchangeHistoriesUseCase {
  List<ProfileExchangeHistory> findProfileExchangeHistoriesOf(long memberId);

  Optional<ProfileExchangeHistory> findProfileExchangeHistoryBetween(
      long memberId, long anotherMemberId);
}
