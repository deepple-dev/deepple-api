package kr.co.deepple.report.application;

import jakarta.transaction.Transactional;
import kr.co.deepple.report.application.port.out.ReportRepository;
import kr.co.deepple.report.domain.ReportTargetType;
import kr.co.deepple.report.event.ReportAccumulatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportAccumulationService {

  static final int HIDE_THRESHOLD = 5;

  private final ReportRepository reportRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void checkAccumulation(ReportTargetType targetType, long targetId) {
    long reporterCount = reportRepository.countByTargetTypeAndTargetId(targetType, targetId);
    if (reporterCount >= HIDE_THRESHOLD) {
      eventPublisher.publishEvent(ReportAccumulatedEvent.of(targetType.name(), targetId));
    }
  }
}
