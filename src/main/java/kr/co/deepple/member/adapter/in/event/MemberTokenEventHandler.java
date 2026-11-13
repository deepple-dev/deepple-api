package kr.co.deepple.member.adapter.in.event;

import kr.co.deepple.auth.application.port.in.RevokeMemberTokensUseCase;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class MemberTokenEventHandler {

  private final RevokeMemberTokensUseCase revokeMemberTokensUseCase;

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(MemberBecameDormantEvent event) {
    revokeMemberTokensUseCase.revokeAll(event.memberId());
  }
}
