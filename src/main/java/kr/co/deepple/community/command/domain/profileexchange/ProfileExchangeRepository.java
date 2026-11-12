package kr.co.deepple.community.command.domain.profileexchange;

import java.util.Optional;

public interface ProfileExchangeRepository {
  ProfileExchange save(ProfileExchange profileExchange);

  boolean existsProfileExchangeBetween(long memberId, long anotherMemberId);

  Optional<ProfileExchange> findById(long id);
}
