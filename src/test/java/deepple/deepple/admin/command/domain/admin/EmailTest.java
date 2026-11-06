package deepple.deepple.admin.command.domain.admin;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {"example@me.com", "user.name@domain.co", "abc.def@ghi.jkl"})
    @DisplayName("유효한 이메일 주소로 Email을 생성할 수 있습니다.")
    void canCreateEmailWhenFormatIsValid(String validEmail) {
        // when
        Email email = Email.from(validEmail);

        // then
        assertThat(email).isNotNull();
        assertThat(email.getAddress()).isEqualTo(validEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"@me.com", "example.com", "example@", "example@me", "홍길동@me.com"})
    @DisplayName("잘못된 형식의 이메일 주소는 INVALID_EMAIL 예외가 발생합니다.")
    void throwsExceptionWhenFormatIsInvalid(String invalidEmail) {
        // when & then
        assertThatThrownBy(() -> Email.from(invalidEmail))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.INVALID_EMAIL);
    }
}