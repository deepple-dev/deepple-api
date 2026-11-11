package deepple.deepple.match.command.infra.match;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.match.command.domain.match.Match;
import deepple.deepple.match.command.domain.match.MatchErrorCode;
import deepple.deepple.match.command.domain.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
                e
            );
        }
    }

    @Override
    public boolean existsActiveMatchBetween(Long memberId, Long anotherMemberId) {
        return matchJpaRepository.existsActiveMatchBetween(memberId, anotherMemberId);
    }

    @Override
    public Optional<Match> findById(Long id) {
        return matchJpaRepository.findById(id);
    }

    @Override
    public Optional<Match> findByIdAndRequesterId(Long id, Long requesterId) {
        return matchJpaRepository.findByIdAndRequesterId(id, requesterId);
    }

    @Override
    public Optional<Match> findByIdAndResponderId(Long id, Long responderId) {
        return matchJpaRepository.findByIdAndResponderId(id, responderId);
    }

    @Override
    public Optional<Match> findByRequesterIdAndResponderId(Long requesterId, Long responderId) {
        return matchJpaRepository.findByRequesterIdAndResponderId(requesterId, responderId);
    }
}
