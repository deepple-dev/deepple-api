package kr.co.deepple.recommendation.application.port.in;

import java.util.Optional;

public interface FindIntroductionTypeUseCase {
  Optional<String> findIntroductionType(long memberId, long introducedMemberId);
}
