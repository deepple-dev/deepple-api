package kr.co.deepple.community.application.port.out;

import java.util.Optional;
import kr.co.deepple.community.domain.ProfileExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfileExchangeRepository extends JpaRepository<ProfileExchange, Long> {
  @Query(
      value =
          """
          SELECT CASE WHEN COUNT(pe) > 0 THEN true ELSE false
          END
          FROM ProfileExchange pe
          WHERE (pe.requesterId = :memberId AND pe.responderId = :anotherMemberId)
          OR (pe.requesterId = :anotherMemberId AND pe.responderId = :memberId)
          """)
  boolean existsProfileExchangeBetween(long memberId, long anotherMemberId);

  @Query(
      value =
          """
          SELECT pe
          FROM ProfileExchange pe
          WHERE (pe.requesterId = :memberId AND pe.responderId = :anotherMemberId)
          OR (pe.requesterId = :anotherMemberId AND pe.responderId = :memberId)
          """)
  Optional<ProfileExchange> findProfileExchangeBetween(long memberId, long anotherMemberId);
}
