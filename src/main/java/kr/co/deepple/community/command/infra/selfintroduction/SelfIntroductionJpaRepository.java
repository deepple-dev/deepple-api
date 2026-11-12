package kr.co.deepple.community.command.infra.selfintroduction;

import kr.co.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelfIntroductionJpaRepository extends JpaRepository<SelfIntroduction, Long> {
  boolean existsByMemberId(Long memberId);
}
