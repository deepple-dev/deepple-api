package kr.co.deepple.admin.application;

import static kr.co.deepple.admin.application.WarningMapper.toWarningReasonTypes;

import java.util.Set;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.admin.application.port.in.IssueWarningCommand;
import kr.co.deepple.admin.application.port.out.WarningRepository;
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

  private final WarningRepository warningRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void issue(long adminId, WarningCreateRequest request) {
    issue(
        adminId,
        new IssueWarningCommand(
            request.memberId(),
            request.reasonTypes(),
            request.isCritical(),
            request.idempotencyKey()));
  }

  @Transactional
  public void issue(long adminId, IssueWarningCommand command) {
    long memberId = command.memberId();
    String idempotencyKey = command.idempotencyKey();
    if (idempotencyKey != null && warningRepository.existsByIdempotencyKey(idempotencyKey)) {
      log.atInfo().addKeyValue("memberId", memberId).log("이미 처리된 경고 요청");
      return;
    }

    long currentWarningCount = warningRepository.countByMemberIdAndIsCriticalTrue(memberId);
    if (command.isCritical()) {
      currentWarningCount += 1;
    }
    Set<WarningReasonType> reasonTypes = toWarningReasonTypes(command.reasonTypes());
    Warning warning =
        Warning.issue(adminId, memberId, reasonTypes, command.isCritical(), idempotencyKey);

    warningRepository.save(warning);
    for (WarningReasonType reasonType : reasonTypes) {
      eventPublisher.publishEvent(
          WarningIssuedEvent.of(
              adminId, memberId, currentWarningCount, reasonType.name(), command.isCritical()));
    }

    log.atInfo()
        .addKeyValue("memberId", memberId)
        .addKeyValue("warningId", warning.getId())
        .log("경고 발행");
  }
}
