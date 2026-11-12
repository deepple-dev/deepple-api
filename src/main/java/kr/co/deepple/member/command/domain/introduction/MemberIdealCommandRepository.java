package kr.co.deepple.member.command.domain.introduction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberIdealCommandRepository extends JpaRepository<MemberIdeal, Long> {
  Optional<MemberIdeal> findByMemberId(Long memberId);

  boolean existsByMemberId(long memberId);
}
