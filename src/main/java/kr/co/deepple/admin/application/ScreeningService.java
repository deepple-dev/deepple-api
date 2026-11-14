package kr.co.deepple.admin.application;

import static kr.co.deepple.admin.application.ScreeningMapper.toRejectionReasonType;

import kr.co.deepple.admin.application.dto.ScreeningApproveRequest;
import kr.co.deepple.admin.application.dto.ScreeningRejectRequest;
import kr.co.deepple.admin.application.port.out.ScreeningCommandRepository;
import kr.co.deepple.admin.domain.AdminErrorCode;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.Screening;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

  private final ScreeningCommandRepository screeningCommandRepository;
  private final DistributedLockTemplate distributedLockTemplate;
  private final ApplicationEventPublisher eventPublisher;

  public void createIfAbsent(long memberId) {
    distributedLockTemplate.executeInTransaction(
        lockKey(memberId),
        () -> {
          if (screeningCommandRepository.existsByMemberId(memberId)) {
            return;
          }
          screeningCommandRepository.save(Screening.from(memberId));
          eventPublisher.publishEvent(ScreeningCreatedEvent.from(memberId));
        });
  }

  @Transactional
  public void approve(long screeningId, ScreeningApproveRequest request, long adminId) {
    Screening screening = getScreening(screeningId);

    if (screening.hasVersionConflict(request.version())) {
      throw new OptimisticLockingFailureException("심사를 승인할 수 없습니다.");
    }

    screening.approve(adminId);
    eventPublisher.publishEvent(ScreeningApprovedEvent.of(adminId, screening.getMemberId()));
    logScreening(screening, "프로필 심사 승인");
  }

  @Transactional
  public void reject(long screeningId, long adminId, ScreeningRejectRequest request) {
    Screening screening = getScreening(screeningId);

    if (screening.hasVersionConflict(request.version())) {
      throw new OptimisticLockingFailureException("심사를 반려할 수 없습니다.");
    }

    RejectionReasonType rejectionReason = toRejectionReasonType(request.rejectionReason());
    screening.reject(adminId, rejectionReason);
    eventPublisher.publishEvent(
        ScreeningRejectedEvent.of(
            adminId, screening.getMemberId(), rejectionReason.getDescription()));
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
          eventPublisher.publishEvent(ScreeningCreatedEvent.from(memberId));
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
