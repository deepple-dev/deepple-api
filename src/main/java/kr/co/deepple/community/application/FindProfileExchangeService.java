package kr.co.deepple.community.application;

import java.util.Optional;
import kr.co.deepple.community.application.port.in.FindProfileExchangeUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindProfileExchangeService implements FindProfileExchangeUseCase {

  private final ProfileExchangeRepository profileExchangeRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<ProfileExchangeSummary> findProfileExchangeBetween(
      long memberId, long anotherMemberId) {
    return profileExchangeRepository
        .findProfileExchangeBetween(memberId, anotherMemberId)
        .map(profileExchange ->
            new ProfileExchangeSummary(
                profileExchange.getId(),
                profileExchange.getRequesterId(),
                profileExchange.getResponderId(),
                profileExchange.getStatus() == null ? null : profileExchange.getStatus().name()));
  }
}
