package kr.co.deepple.match.application;

import java.util.List;
import kr.co.deepple.match.application.port.in.FindMatchHistoriesUseCase;
import kr.co.deepple.match.application.port.in.MatchHistory;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMatchHistoriesService implements FindMatchHistoriesUseCase {

  private final MatchRepository matchRepository;

  @Override
  public List<MatchHistory> findMatchHistoriesOf(long memberId) {
    return matchRepository.findAllOfMember(memberId).stream()
        .map(FindMatchHistoriesService::toHistory)
        .toList();
  }

  @Override
  public List<MatchHistory> findMatchHistoriesBetween(long memberId, long anotherMemberId) {
    return matchRepository.findAllBetween(memberId, anotherMemberId).stream()
        .map(FindMatchHistoriesService::toHistory)
        .toList();
  }

  private static MatchHistory toHistory(Match match) {
    return new MatchHistory(
        match.getId(),
        match.getRequesterId(),
        match.getResponderId(),
        valueOf(match.getRequestMessage()),
        valueOf(match.getResponseMessage()),
        match.getStatus().name(),
        match.getRequesterContactType() == null ? null : match.getRequesterContactType().name(),
        match.getResponderContactType() == null ? null : match.getResponderContactType().name(),
        match.getCreatedAt(),
        match.getRespondedAt(),
        match.getExpiredAt(),
        match.getReadByResponderAt());
  }

  private static String valueOf(Message message) {
    return message == null ? null : message.getValue();
  }
}
