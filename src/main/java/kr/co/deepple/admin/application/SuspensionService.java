package kr.co.deepple.admin.application;

import static kr.co.deepple.admin.application.SuspensionMapper.toSuspensionStatus;

import java.time.Instant;
import java.util.List;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspensionService {

  private final SuspensionRepository suspensionRepository;
  private final SuspensionPolicy suspensionPolicy;
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
    suspensionRepository.findByMemberIdForUpdate(memberId);
    release(memberId);
    log.atInfo().addKeyValue("memberId", memberId).log("회원 정지 해제");
  }

  /**
   * 일시 정지가 만료됐으면 관리자 해제와 같은 방법으로 해제한다. 정지 기록을 잠그고 다시 확인하므로 동시에 여러 번 불려도 한 번만 해제된다. 정지 기록이 없거나 영구
   * 정지이거나 아직 만료되지 않았으면 아무것도 하지 않는다.
   */
  @Transactional
  public void releaseIfExpired(long memberId, Instant now) {
    Optional<Suspension> suspension = suspensionRepository.findByMemberIdForUpdate(memberId);
    if (suspension.isEmpty() || !suspension.get().isExpired(now)) {
      return;
    }
    release(memberId);
    log.atInfo().addKeyValue("memberId", memberId).log("회원 일시 정지 만료 해제");
  }

  @Transactional(readOnly = true)
  public List<Long> findExpiredMemberIds(Instant now) {
    return suspensionRepository.findMemberIdsByStatusAndExpireAtLessThanEqual(
        SuspensionStatus.TEMPORARY, now);
  }

  private void release(long memberId) {
    suspensionRepository.deleteByMemberId(memberId);
    eventPublisher.publishEvent(MemberUnsuspendedEvent.from(memberId));
  }

  private void createOrUpdateSuspension(long adminId, long memberId, SuspensionStatus status) {
    // 자동 해제(releaseIfExpired)와 같은 행을 다루므로 잠그고 읽는다.
    Optional<Suspension> existingSuspension =
        suspensionRepository.findByMemberIdForUpdate(memberId);

    if (status == SuspensionStatus.TEMPORARY) {
      if (existingSuspension.isPresent()) {
        // 만료됐지만 아직 해제되지 않은 일시 정지는 새로 정지한 것처럼 기간을 다시 시작한다.
        if (existingSuspension.get().restartTemporaryIfExpired(adminId, Instant.now())) {
          publishSuspendedEvent(memberId, status);
          logSuspension(memberId, status);
        }
        return;
      }
      Suspension newSuspension = Suspension.createTemporary(adminId, memberId);
      suspensionRepository.save(newSuspension);
      publishSuspendedEvent(memberId, status);
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
}
