package kr.co.deepple.community.adapter.out.persistence;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.ProfileExchange;
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
  public Optional<ProfileExchange> findProfileExchangeBetween(long memberId, long anotherMemberId) {
    return profileExchangeJpaRepository.findProfileExchangeBetween(memberId, anotherMemberId);
  }

  @Override
  public Optional<ProfileExchange> findById(long id) {
    return profileExchangeJpaRepository.findById(id);
  }
}
