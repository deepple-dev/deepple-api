package kr.co.deepple.recommendation.application.port.out;

import java.util.Optional;
import kr.co.deepple.recommendation.domain.MemberIdeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberIdealCommandRepository extends JpaRepository<MemberIdeal, Long> {
  Optional<MemberIdeal> findByMemberId(long memberId);

  boolean existsByMemberId(long memberId);
}
