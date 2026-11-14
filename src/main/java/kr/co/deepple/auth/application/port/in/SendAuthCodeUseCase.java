package kr.co.deepple.auth.application.port.in;

import kr.co.deepple.auth.application.dto.MemberCodeRequest;

public interface SendAuthCodeUseCase {
  void sendAuthCode(MemberCodeRequest request);
}
