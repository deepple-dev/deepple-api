package kr.co.deepple.auth.application;

import java.util.Set;
import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import kr.co.deepple.auth.application.port.out.SmsSender;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthMessageServiceTest {

  @Mock AuthMessageRepository authMessageRepository;

  @Mock SmsSender smsSender;

  AuthMessageService authMessageService;

  @BeforeEach
  void setUp() {
    TestPhoneNumbersProperties testPhoneNumbers =
        new TestPhoneNumbersProperties("135790", Set.of("01000000000"), false);
    authMessageService = new AuthMessageService(authMessageRepository, smsSender, testPhoneNumbers);
  }

  @DisplayName("테스트 번호가 정해진 인증번호를 보내면 저장된 인증번호를 확인하지 않고 통과한다.")
  @Test
  void passesWithTestPhoneNumber() {
    // When
    authMessageService.authenticate("01000000000", "135790");

    // Then
    Mockito.verifyNoInteractions(authMessageRepository);
  }

  @DisplayName("테스트 번호가 아니면 정해진 인증번호를 보내도 저장된 인증번호로 확인한다.")
  @Test
  void checksSavedCodeWhenNotTestPhoneNumber() {
    // Given
    String phoneNumber = "01012345678";

    Mockito.when(authMessageRepository.getByKey(phoneNumber)).thenReturn(null);

    // When & Then
    Assertions.assertThatThrownBy(() -> authMessageService.authenticate(phoneNumber, "135790"))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.AUTH_CODE_NOT_FOUND);
  }

  @DisplayName("해당 번호의 키가 존재하지 않는 경우, 예외 발생.")
  @Test
  void throwsExceptionWhenKeyNotExists() {
    // Given
    String phoneNumber = "01012345678";
    String code = "123456";

    Mockito.when(authMessageRepository.getByKey(phoneNumber)).thenReturn(null);

    // When & Then
    Assertions.assertThatThrownBy(() -> authMessageService.authenticate(phoneNumber, code))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.AUTH_CODE_NOT_FOUND);
  }

  @DisplayName("키가 일치하지 않는 경우, 예외 발생.")
  @Test
  void throwsExceptionWhenCodeNotEqualValue() {
    // Given
    String phoneNumber = "01012345678";
    String code = "123456";
    String value = "654321";

    Mockito.when(authMessageRepository.getByKey(phoneNumber)).thenReturn(value);

    // When & Then
    Assertions.assertThatThrownBy(() -> authMessageService.authenticate(phoneNumber, code))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.AUTH_CODE_MISMATCH);
  }
}
