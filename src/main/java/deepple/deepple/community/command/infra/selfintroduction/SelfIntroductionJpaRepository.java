package deepple.deepple.community.command.infra.selfintroduction;

import deepple.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelfIntroductionJpaRepository extends JpaRepository<SelfIntroduction, Long> {
    boolean existsByMemberId(Long memberId);
}
