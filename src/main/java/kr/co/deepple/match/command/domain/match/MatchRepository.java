package kr.co.deepple.match.command.domain.match;

import java.util.Optional;

public interface MatchRepository {
    void save(Match match);

    boolean existsActiveMatchBetween(Long memberId, Long anotherMemberId);

    Optional<Match> findById(Long id);

    Optional<Match> findByIdAndRequesterId(Long id, Long requesterId);

    Optional<Match> findByIdAndResponderId(Long id, Long responderId);

    Optional<Match> findByRequesterIdAndResponderId(Long requesterId, Long responderId);
}
