package kr.co.deepple.admin.command.application.warning;

import static kr.co.deepple.admin.command.application.warning.WarningMapper.toWarningReasonTypes;

import kr.co.deepple.admin.command.domain.warning.Warning;
import kr.co.deepple.admin.command.domain.warning.WarningCommandRepository;
import kr.co.deepple.admin.presentation.warning.WarningCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarningService {

  private final WarningCommandRepository warningCommandRepository;

  @Transactional
  public void issue(long adminId, WarningCreateRequest request) {
    long memberId = request.memberId();

    long currentWarningCount = warningCommandRepository.countByMemberIdAndIsCriticalTrue(memberId);
    if (request.isCritical()) {
      currentWarningCount += 1;
    }
    var warning =
        Warning.issue(
            adminId,
            memberId,
            currentWarningCount,
            toWarningReasonTypes(request.reasonTypes()),
            request.isCritical());

    warningCommandRepository.save(warning);

    log.atInfo()
        .addKeyValue("memberId", memberId)
        .addKeyValue("warningId", warning.getId())
        .log("경고 발행");
  }
}
