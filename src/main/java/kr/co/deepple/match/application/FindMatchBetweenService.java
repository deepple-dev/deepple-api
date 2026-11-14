package kr.co.deepple.match.application;

import java.util.Optional;
import kr.co.deepple.match.application.port.in.FindMatchBetweenUseCase;
import kr.co.deepple.match.application.port.in.MatchSummary;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMatchBetweenService implements FindMatchBetweenUseCase {

  private final MatchRepository matchRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<MatchSummary> findActiveMatchBetween(long memberId, long anotherMemberId) {
    return matchRepository
        .findActiveMatchBetween(memberId, anotherMemberId)
        .map(FindMatchBetweenService::toSummary);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasReceivedMatchRequest(long responderId, long requesterId) {
    return matchRepository.existsReceivedMatchRequest(responderId, requesterId);
  }

  private static MatchSummary toSummary(Match match) {
    return new MatchSummary(
        match.getId(),
        match.getRequesterId(),
        match.getResponderId(),
        valueOf(match.getRequestMessage()),
        valueOf(match.getResponseMessage()),
        match.getStatus() == null ? null : match.getStatus().name(),
        match.getRequesterContactType() == null ? null : match.getRequesterContactType().name(),
        match.getResponderContactType() == null ? null : match.getResponderContactType().name());
  }

  private static String valueOf(Message message) {
    return message == null ? null : message.getValue();
  }
}
