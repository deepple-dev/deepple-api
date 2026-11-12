package kr.co.deepple.admin.command.application.screening;

import static kr.co.deepple.admin.command.application.screening.ScreeningMapper.toRejectionReasonType;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.screening.Screening;
import kr.co.deepple.admin.command.domain.screening.ScreeningCommandRepository;
import kr.co.deepple.admin.command.domain.screening.ScreeningStatus;
import kr.co.deepple.admin.presentation.screening.ScreeningApproveRequest;
import kr.co.deepple.admin.presentation.screening.ScreeningRejectRequest;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

  private final ScreeningCommandRepository screeningCommandRepository;
  private final DistributedLockTemplate distributedLockTemplate;

  public void createIfAbsent(long memberId) {
    distributedLockTemplate.executeInTransaction(
        lockKey(memberId),
        () -> {
          if (screeningCommandRepository.existsByMemberId(memberId)) {
            return;
          }
          screeningCommandRepository.save(Screening.from(memberId));
        });
  }

  @Transactional
  public void approve(long screeningId, ScreeningApproveRequest request, long adminId) {
    Screening screening = getScreening(screeningId);

    if (screening.hasVersionConflict(request.version())) {
      throw new OptimisticLockingFailureException("심사를 승인할 수 없습니다.");
    }

    screening.approve(adminId);
    logScreening(screening, "프로필 심사 승인");
  }

  @Transactional
  public void reject(long screeningId, long adminId, ScreeningRejectRequest request) {
    Screening screening = getScreening(screeningId);

    if (screening.hasVersionConflict(request.version())) {
      throw new OptimisticLockingFailureException("심사를 반려할 수 없습니다.");
    }

    screening.reject(adminId, toRejectionReasonType(request.rejectionReason()));
    logScreening(screening, "프로필 심사 반려");
  }

  private void logScreening(Screening screening, String message) {
    log.atInfo()
        .addKeyValue("screeningId", screening.getId())
        .addKeyValue("memberId", screening.getMemberId())
        .log(message);
  }

  public void rescreen(long memberId) {
    distributedLockTemplate.executeInTransaction(
        lockKey(memberId),
        () -> {
          if (screeningCommandRepository.existsByMemberIdAndStatus(
              memberId, ScreeningStatus.PENDING)) {
            return;
          }
          screeningCommandRepository.save(Screening.from(memberId));
        });
  }

  private String lockKey(long memberId) {
    return "screening:" + memberId;
  }

  private Screening getScreening(long id) {
    return screeningCommandRepository
        .findById(id)
        .orElseThrow(
            () -> new DeeppleException(AdminErrorCode.SCREENING_NOT_FOUND, "screeningId=" + id));
  }
}
