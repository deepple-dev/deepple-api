package kr.co.deepple.auth.application.port.in;

import kr.co.deepple.auth.application.dto.MemberLoginRequest;
import kr.co.deepple.auth.application.dto.TokenPairResponse;

public interface LoginMemberUseCase {
  TokenPairResponse login(MemberLoginRequest request);
}
