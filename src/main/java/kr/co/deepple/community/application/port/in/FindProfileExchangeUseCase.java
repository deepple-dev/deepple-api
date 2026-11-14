package kr.co.deepple.community.application.port.in;

import java.util.Optional;

public interface FindProfileExchangeUseCase {
  Optional<ProfileExchangeSummary> findProfileExchangeBetween(long memberId, long anotherMemberId);
}
