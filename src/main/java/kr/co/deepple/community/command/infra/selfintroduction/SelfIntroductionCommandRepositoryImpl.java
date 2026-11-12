package kr.co.deepple.community.command.infra.selfintroduction;

import java.util.Optional;
import kr.co.deepple.community.command.domain.selfintroduction.SelfIntroduction;
import kr.co.deepple.community.command.domain.selfintroduction.SelfIntroductionCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SelfIntroductionCommandRepositoryImpl implements SelfIntroductionCommandRepository {

  private final SelfIntroductionJpaRepository selfIntroductionJpaRepository;

  @Override
  public void save(SelfIntroduction selfIntroduction) {
    selfIntroductionJpaRepository.save(selfIntroduction);
  }

  @Override
  public Optional<SelfIntroduction> findById(Long id) {
    return selfIntroductionJpaRepository.findById(id);
  }

  @Override
  public void deleteById(Long id) {
    selfIntroductionJpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByMemberId(Long memberId) {
    return selfIntroductionJpaRepository.existsByMemberId(memberId);
  }
}
