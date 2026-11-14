package kr.co.deepple.match.application.port.out.spi;

import java.util.Optional;

public interface IntroductionTypeFinder {
  Optional<String> findIntroductionType(long memberId, long introducedMemberId);
}
