package deepple.deepple.admin.command.application.screening;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.admin.command.domain.screening.Screening;
import deepple.deepple.admin.command.domain.screening.ScreeningCommandRepository;
import deepple.deepple.admin.presentation.screening.ScreeningApproveRequest;
import deepple.deepple.admin.presentation.screening.ScreeningRejectRequest;
import deepple.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static deepple.deepple.admin.command.application.screening.ScreeningMapper.toRejectionReasonType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningCommandRepository screeningCommandRepository;

    @Transactional
    public void createIfAbsent(long memberId) {
        if (screeningCommandRepository.existsByMemberId(memberId)) {
            return;
        }
        screeningCommandRepository.save(Screening.from(memberId));
    }

    @Transactional
    public void approve(long screeningId, ScreeningApproveRequest request, long adminId) {
        Screening screening = getScreening(screeningId);

        if (screening.hasVersionConflict(request.version())) {
            throw new OptimisticLockingFailureException("심사를 승인할 수 없습니다.");
        }

        screening.approve(adminId);
    }

    @Transactional
    public void reject(long screeningId, long adminId, ScreeningRejectRequest request) {
        Screening screening = getScreening(screeningId);

        if (screening.hasVersionConflict(request.version())) {
            throw new OptimisticLockingFailureException("심사를 반려할 수 없습니다.");
        }

        screening.reject(adminId, toRejectionReasonType(request.rejectionReason()));
    }

    @Transactional
    public void rescreen(long memberId) {
        screeningCommandRepository.save(Screening.from(memberId));
    }

    private Screening getScreening(long id) {
        return screeningCommandRepository.findById(id)
            .orElseThrow(() -> new DeeppleException(AdminErrorCode.SCREENING_NOT_FOUND, "screeningId=" + id));
    }
}
