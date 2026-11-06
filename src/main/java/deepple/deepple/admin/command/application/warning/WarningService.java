package deepple.deepple.admin.command.application.warning;

import deepple.deepple.admin.command.domain.warning.Warning;
import deepple.deepple.admin.command.domain.warning.WarningCommandRepository;
import deepple.deepple.admin.presentation.warning.WarningCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static deepple.deepple.admin.command.application.warning.WarningMapper.toWarningReasonTypes;

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
        var warning = Warning.issue(
            adminId,
            memberId,
            currentWarningCount,
            toWarningReasonTypes(request.reasonTypes()),
            request.isCritical()
        );

        warningCommandRepository.save(warning);

        log.atInfo()
            .addKeyValue("memberId", memberId)
            .addKeyValue("warningId", warning.getId())
            .log("경고 발행");
    }
}
