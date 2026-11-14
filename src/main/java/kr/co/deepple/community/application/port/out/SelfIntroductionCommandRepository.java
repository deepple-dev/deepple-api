package kr.co.deepple.community.application.port.out;

import java.util.Optional;
import kr.co.deepple.community.domain.SelfIntroduction;

public interface SelfIntroductionCommandRepository {
  void save(SelfIntroduction selfIntroduction);

  Optional<SelfIntroduction> findById(long id);

  void deleteById(long id);

  boolean existsByMemberId(long memberId);
}
