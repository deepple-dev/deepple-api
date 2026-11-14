package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.ProfileOptions;
import kr.co.deepple.member.domain.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidateProfileOptionsServiceTest {

  private final ValidateProfileOptionsService validateProfileOptionsService =
      new ValidateProfileOptionsService();

  @Test
  @DisplayName("유효한 값은 대문자 enum 이름으로 정규화해 돌려준다.")
  void normalizeValidOptions() {
    ProfileOptions options =
        new ProfileOptions(Set.of("seoul", "BUSAN"), Set.of("Wine"), "none", "vape", "Social");

    ProfileOptions result = validateProfileOptionsService.validate(options);

    assertThat(result.cities()).containsExactlyInAnyOrder("SEOUL", "BUSAN");
    assertThat(result.hobbies()).containsExactly("WINE");
    assertThat(result.religion()).isEqualTo("NONE");
    assertThat(result.smokingStatus()).isEqualTo("VAPE");
    assertThat(result.drinkingStatus()).isEqualTo("SOCIAL");
  }

  @Test
  @DisplayName("값이 없으면 검증을 통과하고 빈 값으로 돌려준다.")
  void passWhenOptionsAreEmpty() {
    ProfileOptions result =
        validateProfileOptionsService.validate(
            new ProfileOptions(Set.of(), Set.of(), null, "", ""));

    assertThat(result.cities()).isEmpty();
    assertThat(result.hobbies()).isEmpty();
    assertThat(result.religion()).isNull();
    assertThat(result.smokingStatus()).isNull();
    assertThat(result.drinkingStatus()).isNull();
  }

  @Test
  @DisplayName("목록 자체가 없으면 없는 채로 돌려준다.")
  void passWhenCollectionsAreNull() {
    ProfileOptions result =
        validateProfileOptionsService.validate(new ProfileOptions(null, null, null, null, null));

    assertThat(result.cities()).isNull();
    assertThat(result.hobbies()).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"city", "hobby", "religion", "smokingStatus", "drinkingStatus"})
  @DisplayName("enum에 없는 값이 하나라도 있으면 유효하지 않은 프로필 선택 값 예외를 던진다.")
  void throwWhenOptionIsInvalid(String invalidField) {
    ProfileOptions options =
        new ProfileOptions(
            Set.of(invalidField.equals("city") ? "MARS" : "SEOUL"),
            Set.of(invalidField.equals("hobby") ? "SLEEPING" : "WINE"),
            invalidField.equals("religion") ? "UNKNOWN" : "NONE",
            invalidField.equals("smokingStatus") ? "UNKNOWN" : "NONE",
            invalidField.equals("drinkingStatus") ? "UNKNOWN" : "NONE");

    assertThatThrownBy(() -> validateProfileOptionsService.validate(options))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_PROFILE_OPTION);
  }
}
