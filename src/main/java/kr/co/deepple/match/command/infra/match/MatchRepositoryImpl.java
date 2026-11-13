package kr.co.deepple.match.command.infra.match;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchErrorCode;
import kr.co.deepple.match.command.domain.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepository {

  private final MatchJpaRepository matchJpaRepository;

  @Override
  public void save(Match match) {
    try {
      matchJpaRepository.save(match);
    } catch (DataIntegrityViolationException e) {
      throw new DeeppleException(
          MatchErrorCode.MATCH_ALREADY_EXISTS,
          "requesterId=" + match.getRequesterId() + ", responderId=" + match.getResponderId(),
          e);
    }
  }

  @Override
  public boolean existsActiveMatchBetween(long memberId, long anotherMemberId) {
    return matchJpaRepository.existsActiveMatchBetween(memberId, anotherMemberId);
  }

  @Override
  public Optional<Match> findById(long id) {
    return matchJpaRepository.findById(id);
  }

  @Override
  public Optional<Match> findByIdAndRequesterId(long id, long requesterId) {
    return matchJpaRepository.findByIdAndRequesterId(id, requesterId);
  }

  @Override
  public Optional<Match> findByIdAndResponderId(long id, long responderId) {
    return matchJpaRepository.findByIdAndResponderId(id, responderId);
  }
}
