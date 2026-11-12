package kr.co.deepple.community.command.infra.profileexchange;

import kr.co.deepple.community.command.domain.profileexchange.ProfileExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileExchangeJpaRepository extends JpaRepository<ProfileExchange, Long> {
  @Query(
      value =
          """
          SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false
          END
          FROM ProfileExchange pe
          WHERE (pe.requesterId = :memberId AND pe.responderId = :anotherMemberId)
          OR (pe.requesterId = :anotherMemberId AND pe.responderId = :memberId)
          """)
  boolean existsProfileExchangeBetween(Long memberId, Long anotherMemberId);
}
