package kr.co.deepple.admin.adapter.in.spi;

import java.util.Optional;
import kr.co.deepple.admin.application.ScreeningQueryService;
import kr.co.deepple.member.application.port.out.spi.ScreeningRejection;
import kr.co.deepple.member.application.port.out.spi.ScreeningRejectionFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberScreeningRejectionFinder implements ScreeningRejectionFinder {

  private final ScreeningQueryService screeningQueryService;

  @Override
  public Optional<ScreeningRejection> findLatestRejection(long memberId) {
    return screeningQueryService
        .findLatestRejectionReason(memberId)
        .map(reason -> new ScreeningRejection(reason.name(), reason.getDescription()));
  }
}
