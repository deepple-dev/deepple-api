package deepple.deepple.community.command.domain.selfintroduction;

import java.util.Optional;

public interface SelfIntroductionCommandRepository {
    void save(SelfIntroduction selfIntroduction);

    Optional<SelfIntroduction> findById(Long id);

    void deleteById(Long id);

    boolean existsByMemberId(Long memberId);
}
