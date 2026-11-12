package kr.co.deepple.admin.command.application.suspension;

import static kr.co.deepple.admin.command.application.suspension.SuspensionMapper.toSuspensionStatus;

import java.time.Instant;
import java.util.Optional;
import kr.co.deepple.admin.command.domain.suspension.MemberUnsuspendedEvent;
import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import kr.co.deepple.admin.command.domain.suspension.SuspensionPolicy;
import kr.co.deepple.admin.command.domain.suspension.SuspensionStatus;
import kr.co.deepple.admin.presentation.suspension.SuspendRequest;
import kr.co.deepple.common.event.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspensionService {

  private final SuspensionCommandRepository suspensionCommandRepository;
  private final SuspensionPolicy suspensionPolicy;
  private final TaskScheduler taskScheduler;

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
    suspensionCommandRepository.deleteByMemberId(memberId);
    Events.raise(MemberUnsuspendedEvent.from(memberId));
    log.atInfo().addKeyValue("memberId", memberId).log("회원 정지 해제");
  }

  private void createOrUpdateSuspension(long adminId, long memberId, SuspensionStatus status) {
    Optional<Suspension> existingSuspension = suspensionCommandRepository.findByMemberId(memberId);

    if (status == SuspensionStatus.TEMPORARY) {
      if (existingSuspension.isPresent()) {
        return;
      }
      Suspension newSuspension = Suspension.createTemporary(adminId, memberId);
      suspensionCommandRepository.save(newSuspension);

      registerTemporaryScheduler(newSuspension.getMemberId(), newSuspension.getExpireAt());
      logSuspension(memberId, status);
    }

    if (status == SuspensionStatus.PERMANENT) {
      existingSuspension.ifPresentOrElse(
          suspension -> suspension.changeToPermanent(adminId),
          () -> {
            Suspension newSuspension = Suspension.createPermanent(adminId, memberId);
            suspensionCommandRepository.save(newSuspension);
          });
      logSuspension(memberId, status);
    }
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
          Optional<Suspension> suspension = suspensionCommandRepository.findByMemberId(memberId);
          if (suspension.isEmpty() || suspension.get().getStatus() == SuspensionStatus.PERMANENT) {
            return;
          }

          suspensionCommandRepository.deleteByMemberId(memberId);
          Events.raise(MemberUnsuspendedEvent.from(memberId));
        },
        expireAt);
  }
}
