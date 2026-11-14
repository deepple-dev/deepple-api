package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.port.in.RevokeMemberTokensUseCase;
import kr.co.deepple.auth.domain.TokenOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RevokeMemberTokensService implements RevokeMemberTokensUseCase {

  private final RefreshTokenService refreshTokenService;

  @Override
  public void revokeAll(long memberId) {
    refreshTokenService.revokeAll(TokenOwner.member(memberId));
  }
}
