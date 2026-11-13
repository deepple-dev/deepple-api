package kr.co.deepple.admin.infra.suspension;

import java.util.Optional;
import kr.co.deepple.admin.command.domain.suspension.Suspension;
import kr.co.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.auth.application.port.in.SuspendMemberAccountUseCase;
import kr.co.deepple.auth.application.port.in.UnsuspendMemberAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAccountSuspensionEventHandler {

  private final SuspensionCommandRepository suspensionCommandRepository;
  private final SuspendMemberAccountUseCase suspendMemberAccountUseCase;
  private final UnsuspendMemberAccountUseCase unsuspendMemberAccountUseCase;

  @ApplicationModuleListener
  public void handle(MemberSuspendedEvent event) {
    syncAccount(event.memberId());
  }

  @ApplicationModuleListener
  public void handle(MemberUnsuspendedEvent event) {
    syncAccount(event.memberId());
  }

  private void syncAccount(long memberId) {
    Optional<Suspension> suspension = suspensionCommandRepository.findByMemberId(memberId);
    if (suspension.isEmpty()) {
      unsuspendMemberAccountUseCase.unsuspend(memberId);
      return;
    }
    Suspension current = suspension.get();
    switch (current.getStatus()) {
      case TEMPORARY ->
          suspendMemberAccountUseCase.suspendTemporarily(memberId, current.getExpireAt());
      case PERMANENT -> suspendMemberAccountUseCase.suspendPermanently(memberId);
      default -> throw new IllegalStateException("정지 기록의 상태를 알 수 없습니다. memberId=" + memberId);
    }
  }
}
