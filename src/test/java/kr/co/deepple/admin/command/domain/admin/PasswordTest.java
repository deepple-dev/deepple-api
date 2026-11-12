package kr.co.deepple.admin.command.domain.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordTest {

  @Mock private PasswordHasher passwordHasher;

  @Nested
  @DisplayName("비밀번호 유효성 검증")
  class PasswordValidation {

    @ParameterizedTest
    @ValueSource(strings = {"pw345678^^", "password9012345678^^"})
    @DisplayName("10자 이상 20자 이하의 문자, 숫자, 특수문자의 조합으로 Password를 생성할 수 있습니다.")
    void canCreatePasswordWhenFormatIsValid(String validPassword) {
      // given
      when(passwordHasher.hash(anyString()))
          .thenAnswer(invocation -> "hashed" + invocation.getArgument(0, String.class));

      // when
      Password password = Password.fromRaw(validPassword, passwordHasher);

      // then
      assertThat(password).isNotNull();
      assertThat(password.getHashedValue()).isEqualTo("hashed" + validPassword);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "pw123^^",
          "abcdefghijklmnopqrstuvwxyz1234567890^^",
          "1234567890^^",
          "password^^",
          "password1234",
          "비밀번호abcd1234^^"
        })
    @DisplayName("잘못된 형식의 비밀번호는 INVALID_PASSWORD 예외가 발생합니다.")
    void createPasswordWhenInvalidThrowsException(String invalidPassword) {
      // when & then
      assertThatThrownBy(() -> Password.fromRaw(invalidPassword, passwordHasher))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.INVALID_PASSWORD);
    }
  }

  @Nested
  @DisplayName("비밀번호 매칭")
  class PasswordMatch {

    @Test
    @DisplayName("비밀번호가 일치하는 경우 true를 반환합니다.")
    void shouldReturnTrueWhenPasswordMatches() {
      // given
      String rawPassword = "raw";
      String hashedPassword = "hashed";

      Password password = Password.fromHashed(hashedPassword);
      when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(true);

      // when
      boolean result = password.matches(rawPassword, passwordHasher);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않는 경우 false를 반환합니다.")
    void shouldReturnFalseWhenPasswordDoesntMatch() {
      // given
      String rawPassword = "raw";
      String hashedPassword = "hashed";

      Password password = Password.fromHashed(hashedPassword);
      when(passwordHasher.matches(rawPassword, hashedPassword)).thenReturn(false);

      // when
      boolean result = password.matches(rawPassword, passwordHasher);

      // then
      assertThat(result).isFalse();
    }
  }
}
