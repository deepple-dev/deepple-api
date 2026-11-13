package kr.co.deepple.auth.application;

import java.time.Instant;
import kr.co.deepple.auth.application.dto.TokenPairResponse;
import kr.co.deepple.auth.application.port.in.RefreshMemberTokenUseCase;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.auth.event.MemberAccountTokenRefreshedEvent;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshMemberTokenService implements RefreshMemberTokenUseCase {

  private final MemberAccountRepository memberAccountRepository;
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public TokenPairResponse refresh(String refreshToken) {
    TokenOwner owner = refreshTokenService.consume(refreshToken);
    if (!owner.isMember()) {
      throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
    Instant now = Instant.now();
    validateRefreshAllowed(owner, now);

    String newAccessToken = tokenProvider.createAccessToken(owner.id(), Role.MEMBER, now);
    String newRefreshToken = refreshTokenService.issue(owner);
    eventPublisher.publishEvent(MemberAccountTokenRefreshedEvent.from(owner.id()));

    return new TokenPairResponse(newAccessToken, newRefreshToken);
  }

  private void validateRefreshAllowed(TokenOwner owner, Instant now) {
    MemberAccount account = memberAccountRepository.findById(owner.id()).orElse(null);
    if (account == null) {
      refreshTokenService.revokeAll(owner);
      throw new DeeppleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    try {
      account.validateLoginAllowed(now);
    } catch (DeeppleException e) {
      refreshTokenService.revokeAll(owner);
      throw e;
    }
  }
}
