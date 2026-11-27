package kr.co.deepple.member.application.port.out.spi;

import java.util.Optional;

public interface ScreeningRejectionFinder {
  Optional<ScreeningRejection> findLatestRejection(long memberId);
}
