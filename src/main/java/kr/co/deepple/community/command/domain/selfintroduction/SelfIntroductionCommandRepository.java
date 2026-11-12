package kr.co.deepple.community.command.domain.selfintroduction;

import java.util.Optional;

public interface SelfIntroductionCommandRepository {
  void save(SelfIntroduction selfIntroduction);

  Optional<SelfIntroduction> findById(long id);

  void deleteById(long id);

  boolean existsByMemberId(long memberId);
}
