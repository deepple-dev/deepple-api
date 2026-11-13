package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.auth.event.MemberAccountDeletedEvent;
import kr.co.deepple.auth.event.MemberAccountLoggedInEvent;
import kr.co.deepple.auth.event.MemberAccountTokenRefreshedEvent;
import kr.co.deepple.member.command.application.member.MemberAccountSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAccountEventHandler {

  private final MemberAccountSyncService memberAccountSyncService;

  @ApplicationModuleListener
  public void handle(MemberAccountLoggedInEvent event) {
    memberAccountSyncService.recordLogin(event.accountId(), event.metadata().occurredAt());
  }

  @EventListener
  public void handle(MemberAccountDeletedEvent event) {
    memberAccountSyncService.delete(event.accountId());
  }

  @ApplicationModuleListener
  public void handle(MemberAccountTokenRefreshedEvent event) {
    memberAccountSyncService.recordAccess(event.accountId(), event.metadata().occurredAt());
  }
}
