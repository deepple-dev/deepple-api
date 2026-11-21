package kr.co.deepple.feed.application.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.domain.FeedErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("관리자 피드 정렬 키 테스트")
class OfficialFeedSortKeyTest {

  @Test
  @DisplayName("커서로 바꿨다가 다시 읽으면 고정 여부, 공개 시각(마이크로초), ID가 그대로다")
  void cursorRoundTrip() {
    OfficialFeedSortKey cursor =
        new OfficialFeedSortKey(true, LocalDateTime.of(2026, 11, 1, 9, 0, 0, 123_456_000), 15L);

    OfficialFeedSortKey parsed = OfficialFeedSortKey.parse(cursor.toCursor()).orElseThrow();

    assertThat(cursor.toCursor()).isEqualTo("true_2026-11-01T09:00:00.123456_15");
    assertThat(parsed).isEqualTo(cursor);
  }

  @Test
  @DisplayName("초 이하가 0인 공개 시각도 그대로 읽는다")
  void cursorRoundTripWithoutSeconds() {
    OfficialFeedSortKey cursor =
        new OfficialFeedSortKey(false, LocalDateTime.of(2026, 11, 1, 9, 0), 3L);

    assertThat(OfficialFeedSortKey.parse(cursor.toCursor())).contains(cursor);
  }

  @Test
  @DisplayName("커서가 없으면 첫 페이지라 빈 값이다")
  void emptyCursorIsFirstPage() {
    assertThat(OfficialFeedSortKey.parse(null)).isEmpty();
    assertThat(OfficialFeedSortKey.parse(" ")).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "abc",
        "true_2026-11-01T09:00_15_1",
        "yes_2026-11-01T09:00_15",
        "true_2026-13-01T09:00_15",
        "true_2026-11-01T09:00_x",
        "true_2026-11-01T09:00_0"
      })
  @DisplayName("형식이 틀린 커서는 거절한다")
  void rejectInvalidCursor(String cursor) {
    assertThatThrownBy(() -> OfficialFeedSortKey.parse(cursor))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_CURSOR);
  }
}
