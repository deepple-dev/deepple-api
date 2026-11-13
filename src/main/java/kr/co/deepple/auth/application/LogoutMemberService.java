package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.port.in.LogoutMemberUseCase;
import kr.co.deepple.auth.event.MemberAccountLoggedOutEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutMemberService implements LogoutMemberUseCase {

  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public void logout(long memberId, String refreshToken) {
    eventPublisher.publishEvent(MemberAccountLoggedOutEvent.from(memberId));
    refreshTokenService.revoke(refreshToken);
  }
}
