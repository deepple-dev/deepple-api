package kr.co.deepple.match.adapter.out.persistence;

import java.util.Optional;
import kr.co.deepple.match.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchJpaRepository extends JpaRepository<Match, Long> {
  @Query(
      value =
          """
          SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false
          END
          FROM Match m
          WHERE ((m.requesterId = :memberId AND m.responderId = :anotherMemberId)
          OR (m.requesterId = :anotherMemberId AND m.responderId = :memberId))
          AND m.status <> 'EXPIRED' AND m.status <> 'REJECT_CHECKED'
          """)
  boolean existsActiveMatchBetween(long memberId, long anotherMemberId);

  Optional<Match> findByIdAndRequesterId(long id, long requesterId);

  Optional<Match> findByIdAndResponderId(long id, long responderId);
}
