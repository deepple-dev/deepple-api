package kr.co.deepple.match.adapter.out.persistence;

import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchErrorCode;
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

  @Override
  public Set<Long> findRequesterIdsByResponderId(long responderId) {
    return matchJpaRepository.findRequesterIdsByResponderId(responderId);
  }

  @Override
  public Set<Long> findResponderIdsByRequesterId(long requesterId) {
    return matchJpaRepository.findResponderIdsByRequesterId(requesterId);
  }
}
