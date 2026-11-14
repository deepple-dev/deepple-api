package kr.co.deepple.recommendation.application;

import java.util.Optional;
import kr.co.deepple.recommendation.application.port.in.FindIntroductionTypeUseCase;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindIntroductionTypeService implements FindIntroductionTypeUseCase {

  private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;

  @Override
  @Transactional(readOnly = true)
  public Optional<String> findIntroductionType(long memberId, long introducedMemberId) {
    return memberIntroductionCommandRepository
        .findByMemberIdAndIntroducedMemberId(memberId, introducedMemberId)
        .map(MemberIntroduction::getType)
        .map(IntroductionType::name);
  }
}
