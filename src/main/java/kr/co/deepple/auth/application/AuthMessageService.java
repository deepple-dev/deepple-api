package kr.co.deepple.auth.application;

import java.security.SecureRandom;
import java.util.Random;
import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import kr.co.deepple.auth.application.port.out.SmsSender;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMessageService {

  private static final Random generator = new SecureRandom();
  private final AuthMessageRepository authMessageRepository;
  private final SmsSender smsSender;
  private final TestPhoneNumbersProperties testPhoneNumbers;

  public void sendAndSaveCode(String phoneNumber) {
    String code = generateNumber();
    authMessageRepository.save(phoneNumber, code);
    smsSender.sendMessage(getMessage(code), phoneNumber);
  }

  public void authenticate(String phoneNumber, String code) {
    if (testPhoneNumbers.accepts(phoneNumber, code)) {
      return;
    }

    String value = authMessageRepository.getByKey(phoneNumber);
    validateWithCode(value, code);
    authMessageRepository.delete(phoneNumber);
  }

  private String getMessage(String code) {
    return "[딥플] 인증번호 [" + code + "]를 입력해주세요.";
  }

  private String generateNumber() {
    return String.format("%06d", (generator.nextInt(1000000)));
  }

  private void validateWithCode(String value, String code) {
    if (value == null) {
      throw new DeeppleException(AuthErrorCode.AUTH_CODE_NOT_FOUND);
    }

    if (!value.equals(code)) {
      throw new DeeppleException(AuthErrorCode.AUTH_CODE_MISMATCH);
    }
  }
}
