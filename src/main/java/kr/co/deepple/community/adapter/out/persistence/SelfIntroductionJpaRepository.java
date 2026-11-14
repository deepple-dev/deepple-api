package kr.co.deepple.community.adapter.out.persistence;

import kr.co.deepple.community.domain.SelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelfIntroductionJpaRepository extends JpaRepository<SelfIntroduction, Long> {
  boolean existsByMemberId(long memberId);
}
