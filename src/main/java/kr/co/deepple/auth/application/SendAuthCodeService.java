package kr.co.deepple.auth.application;

import kr.co.deepple.auth.application.dto.MemberCodeRequest;
import kr.co.deepple.auth.application.port.in.SendAuthCodeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendAuthCodeService implements SendAuthCodeUseCase {

  private final AuthMessageService authMessageService;

  @Override
  public void sendAuthCode(MemberCodeRequest request) {
    authMessageService.sendAndSaveCode(request.phoneNumber());
  }
}
