package kr.co.deepple.community.command.infra.profileexchange;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchange;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfileExchangeRepositoryImpl implements ProfileExchangeRepository {

  private final ProfileExchangeJpaRepository profileExchangeJpaRepository;

  @Override
  public ProfileExchange save(ProfileExchange profileExchange) {
    try {
      return profileExchangeJpaRepository.save(profileExchange);
    } catch (DataIntegrityViolationException e) {
      throw new DeeppleException(
          CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS,
          "requesterId="
              + profileExchange.getRequesterId()
              + ", responderId="
              + profileExchange.getResponderId(),
          e);
    }
  }

  @Override
  public boolean existsProfileExchangeBetween(long memberId, long anotherMemberId) {
    return profileExchangeJpaRepository.existsProfileExchangeBetween(memberId, anotherMemberId);
  }

  @Override
  public Optional<ProfileExchange> findById(long id) {
    return profileExchangeJpaRepository.findById(id);
  }
}
