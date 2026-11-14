package kr.co.deepple.community.application.port.out;

import kr.co.deepple.community.domain.SelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelfIntroductionRepository extends JpaRepository<SelfIntroduction, Long> {
  boolean existsByMemberId(long memberId);
}
