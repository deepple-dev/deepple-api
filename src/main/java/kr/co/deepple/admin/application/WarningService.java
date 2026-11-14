package kr.co.deepple.admin.application;

import static kr.co.deepple.admin.application.WarningMapper.toWarningReasonTypes;

import java.util.Set;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.admin.application.port.out.WarningCommandRepository;
import kr.co.deepple.admin.domain.Warning;
import kr.co.deepple.admin.domain.WarningReasonType;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningService {

  private final WarningCommandRepository warningCommandRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void issue(long adminId, WarningCreateRequest request) {
    long memberId = request.memberId();
    String idempotencyKey = request.idempotencyKey();
    if (idempotencyKey != null && warningCommandRepository.existsByIdempotencyKey(idempotencyKey)) {
      log.atInfo().addKeyValue("memberId", memberId).log("이미 처리된 경고 요청");
      return;
    }

    long currentWarningCount = warningCommandRepository.countByMemberIdAndIsCriticalTrue(memberId);
    if (request.isCritical()) {
      currentWarningCount += 1;
    }
    Set<WarningReasonType> reasonTypes = toWarningReasonTypes(request.reasonTypes());
    Warning warning =
        Warning.issue(adminId, memberId, reasonTypes, request.isCritical(), idempotencyKey);

    warningCommandRepository.save(warning);
    for (WarningReasonType reasonType : reasonTypes) {
      eventPublisher.publishEvent(
          WarningIssuedEvent.of(
              adminId, memberId, currentWarningCount, reasonType.name(), request.isCritical()));
    }

    log.atInfo()
        .addKeyValue("memberId", memberId)
        .addKeyValue("warningId", warning.getId())
        .log("경고 발행");
  }
}
