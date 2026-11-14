package kr.co.deepple.match.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchContactTypeTest {

  @Test
  @DisplayName("이름이 일치하면 연락 수단을 반환한다.")
  void returnContactType() {
    assertThat(MatchContactType.from("KAKAO")).isEqualTo(MatchContactType.KAKAO);
  }

  @Test
  @DisplayName("없는 연락 수단이면 INVALID_MATCH_CONTACT_TYPE 예외를 던진다.")
  void throwWhenUnknownContactType() {
    assertThatThrownBy(() -> MatchContactType.from("EMAIL"))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.INVALID_MATCH_CONTACT_TYPE);
  }
}
