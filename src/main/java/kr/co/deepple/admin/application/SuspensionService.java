package kr.co.deepple.admin.application;

import static kr.co.deepple.admin.application.SuspensionMapper.toSuspensionStatus;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.admin.application.dto.SuspendRequest;
import kr.co.deepple.admin.application.port.out.SuspensionRepository;
import kr.co.deepple.admin.domain.Suspension;
import kr.co.deepple.admin.domain.SuspensionPolicy;
import kr.co.deepple.admin.domain.SuspensionStatus;
import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspensionService {

  private final SuspensionRepository suspensionRepository;
  private final SuspensionPolicy suspensionPolicy;
  private final TaskScheduler taskScheduler;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void suspendByAdmin(long adminId, long memberId, SuspendRequest request) {
    SuspensionStatus requestedStatus = toSuspensionStatus(request.status());
    createOrUpdateSuspension(adminId, memberId, requestedStatus);
  }

  @Transactional
  public void suspendByWarningCount(long adminId, long memberId, long currentWarningCount) {
    SuspensionStatus evaluatedStatus = suspensionPolicy.evaluate(currentWarningCount);
    createOrUpdateSuspension(adminId, memberId, evaluatedStatus);
  }

  @Transactional
  public void delete(long memberId) {
    suspensionRepository.deleteByMemberId(memberId);
    eventPublisher.publishEvent(MemberUnsuspendedEvent.from(memberId));
    log.atInfo().addKeyValue("memberId", memberId).log("회원 정지 해제");
  }

  private void createOrUpdateSuspension(long adminId, long memberId, SuspensionStatus status) {
    Optional<Suspension> existingSuspension = suspensionRepository.findByMemberId(memberId);

    if (status == SuspensionStatus.TEMPORARY) {
      if (existingSuspension.isPresent()) {
        return;
      }
      Suspension newSuspension = Suspension.createTemporary(adminId, memberId);
      suspensionRepository.save(newSuspension);
      publishSuspendedEvent(memberId, status);

      registerTemporaryScheduler(newSuspension.getMemberId(), newSuspension.getExpireAt());
      logSuspension(memberId, status);
    }

    if (status == SuspensionStatus.PERMANENT) {
      existingSuspension.ifPresentOrElse(
          suspension -> {
            if (suspension.changeToPermanent(adminId)) {
              publishSuspendedEvent(memberId, status);
            }
          },
          () -> {
            Suspension newSuspension = Suspension.createPermanent(adminId, memberId);
            suspensionRepository.save(newSuspension);
            publishSuspendedEvent(memberId, status);
          });
      logSuspension(memberId, status);
    }
  }

  private void publishSuspendedEvent(long memberId, SuspensionStatus status) {
    eventPublisher.publishEvent(MemberSuspendedEvent.of(memberId, status.name()));
  }

  private void logSuspension(long memberId, SuspensionStatus status) {
    log.atInfo()
        .addKeyValue("memberId", memberId)
        .addKeyValue("suspensionStatus", status)
        .log("회원 정지");
  }

  private void registerTemporaryScheduler(long memberId, Instant expireAt) {
    if (expireAt == null || expireAt.isBefore(Instant.now())) {
      return;
    }

    taskScheduler.schedule(
        () -> {
          Optional<Suspension> suspension = suspensionRepository.findByMemberId(memberId);
          if (suspension.isEmpty() || suspension.get().getStatus() == SuspensionStatus.PERMANENT) {
            return;
          }

          suspensionRepository.deleteByMemberId(memberId);
          eventPublisher.publishEvent(MemberUnsuspendedEvent.from(memberId));
        },
        expireAt);
  }
}
