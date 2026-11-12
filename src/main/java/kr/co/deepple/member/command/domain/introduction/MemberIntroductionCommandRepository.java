package kr.co.deepple.member.command.domain.introduction;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberIntroductionCommandRepository
    extends JpaRepository<MemberIntroduction, Long> {
  boolean existsByMemberIdAndIntroducedMemberId(long memberId, long introducedMemberId);

  Optional<MemberIntroduction> findByMemberIdAndIntroducedMemberId(
      long memberId, long introducedMemberId);

  List<MemberIntroduction> findAllByMemberIdAndIntroducedMemberIdIn(
      long memberId, Set<Long> introducedMemberIds);
}
