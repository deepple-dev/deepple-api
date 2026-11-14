package kr.co.deepple.recommendation.adapter.in.spi;

import java.util.Optional;
import kr.co.deepple.match.application.port.out.spi.IntroductionTypeFinder;
import kr.co.deepple.recommendation.application.port.in.FindIntroductionTypeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchIntroductionTypeFinder implements IntroductionTypeFinder {

  private final FindIntroductionTypeUseCase findIntroductionTypeUseCase;

  @Override
  public Optional<String> findIntroductionType(long memberId, long introducedMemberId) {
    return findIntroductionTypeUseCase.findIntroductionType(memberId, introducedMemberId);
  }
}
