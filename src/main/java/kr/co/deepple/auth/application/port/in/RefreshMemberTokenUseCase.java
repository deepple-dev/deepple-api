package kr.co.deepple.auth.application.port.in;

import kr.co.deepple.auth.application.dto.TokenPairResponse;

public interface RefreshMemberTokenUseCase {
  TokenPairResponse refresh(String refreshToken);
}
