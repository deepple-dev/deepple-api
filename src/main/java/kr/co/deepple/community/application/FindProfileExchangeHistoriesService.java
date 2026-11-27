package kr.co.deepple.community.application;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.community.application.port.in.FindProfileExchangeHistoriesUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeHistory;
import kr.co.deepple.community.application.port.out.ProfileExchangeRepository;
import kr.co.deepple.community.domain.ProfileExchange;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindProfileExchangeHistoriesService implements FindProfileExchangeHistoriesUseCase {

  private final ProfileExchangeRepository profileExchangeRepository;

  @Override
  public List<ProfileExchangeHistory> findProfileExchangeHistoriesOf(long memberId) {
    return profileExchangeRepository.findAllOfMember(memberId).stream()
        .map(FindProfileExchangeHistoriesService::toHistory)
        .toList();
  }

  @Override
  public Optional<ProfileExchangeHistory> findProfileExchangeHistoryBetween(
      long memberId, long anotherMemberId) {
    return profileExchangeRepository
        .findProfileExchangeBetween(memberId, anotherMemberId)
        .map(FindProfileExchangeHistoriesService::toHistory);
  }

  private static ProfileExchangeHistory toHistory(ProfileExchange exchange) {
    return new ProfileExchangeHistory(
        exchange.getId(),
        exchange.getRequesterId(),
        exchange.getResponderId(),
        exchange.getStatus().name(),
        exchange.getCreatedAt(),
        exchange.getStatus() == ProfileExchangeStatus.WAITING ? null : exchange.getUpdatedAt());
  }
}
