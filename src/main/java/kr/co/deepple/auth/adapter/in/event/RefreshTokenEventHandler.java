package kr.co.deepple.auth.adapter.in.event;

import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.auth.application.RefreshTokenService;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class RefreshTokenEventHandler {

  private final RefreshTokenService refreshTokenService;

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(MemberSuspendedEvent event) {
    refreshTokenService.revokeAll(TokenOwner.member(event.memberId()));
  }

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(MemberBecameDormantEvent event) {
    refreshTokenService.revokeAll(TokenOwner.member(event.memberId()));
  }
}
