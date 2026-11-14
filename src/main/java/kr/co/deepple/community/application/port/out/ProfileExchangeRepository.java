package kr.co.deepple.community.application.port.out;

import java.util.Optional;
import kr.co.deepple.community.domain.ProfileExchange;

public interface ProfileExchangeRepository {
  ProfileExchange save(ProfileExchange profileExchange);

  boolean existsProfileExchangeBetween(long memberId, long anotherMemberId);

  Optional<ProfileExchange> findProfileExchangeBetween(long memberId, long anotherMemberId);

  Optional<ProfileExchange> findById(long id);
}
