package kr.co.deepple.member.adapter.in.event;

import kr.co.deepple.auth.event.MemberAccountDeletedEvent;
import kr.co.deepple.member.application.MemberAccountSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAccountEventHandler {

  private final MemberAccountSyncService memberAccountSyncService;

  @EventListener
  public void handle(MemberAccountDeletedEvent event) {
    memberAccountSyncService.delete(event.accountId());
  }
}
