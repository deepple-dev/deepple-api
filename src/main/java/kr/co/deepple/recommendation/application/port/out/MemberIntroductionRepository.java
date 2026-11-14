package kr.co.deepple.recommendation.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberIntroductionRepository extends JpaRepository<MemberIntroduction, Long> {
  boolean existsByMemberIdAndIntroducedMemberId(long memberId, long introducedMemberId);

  Optional<MemberIntroduction> findByMemberIdAndIntroducedMemberId(
      long memberId, long introducedMemberId);

  List<MemberIntroduction> findAllByMemberIdAndIntroducedMemberIdIn(
      long memberId, Set<Long> introducedMemberIds);
}
