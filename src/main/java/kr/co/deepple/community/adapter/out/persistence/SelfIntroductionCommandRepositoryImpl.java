package kr.co.deepple.community.adapter.out.persistence;

import java.util.Optional;
import kr.co.deepple.community.application.port.out.SelfIntroductionCommandRepository;
import kr.co.deepple.community.domain.SelfIntroduction;
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
  public Optional<SelfIntroduction> findById(long id) {
    return selfIntroductionJpaRepository.findById(id);
  }

  @Override
  public void deleteById(long id) {
    selfIntroductionJpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByMemberId(long memberId) {
    return selfIntroductionJpaRepository.existsByMemberId(memberId);
  }
}
