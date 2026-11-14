package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class KakaoIdTest {

  @Test
  @DisplayName("길이가 3글자 미만인 경우, 유효하지 않습니다.")
  void isInvalidWhenIdLengthIsLessThan3() {
    // Given
    String kakaoId = "ab";

    // When & Then
    Assertions.assertThatThrownBy(() -> KakaoId.from(kakaoId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_KAKAO_ID);
  }

  @Test
  @DisplayName("패턴과 일치하지 않은 경우, 유효하지 않습니다.")
  void isInvalidWhenIdIsNotMatchedByPattern() {
    // Given
    String kakaoId = "abcd&*";

    // When & Then
    Assertions.assertThatThrownBy(() -> KakaoId.from(kakaoId))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_KAKAO_ID);
  }

  @Test
  @DisplayName("패턴과 일치하는 경우, 유효합니다.")
  void isValidWhenIdIsMatchedByPattern() {
    // Given
    String value = "abcd_";

    // When
    KakaoId kakaoId = KakaoId.from(value);

    // Then
    Assertions.assertThat(kakaoId.getValue()).isEqualTo(value);
  }
}
