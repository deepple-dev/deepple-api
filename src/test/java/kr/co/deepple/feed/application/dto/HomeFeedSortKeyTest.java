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

@DisplayName("홈 피드 정렬 키 테스트")
class HomeFeedSortKeyTest {

  private static final LocalDateTime OLD = LocalDateTime.of(2026, 1, 1, 0, 0, 0, 123_456_000);
  private static final LocalDateTime NEW = LocalDateTime.of(2026, 11, 21, 9, 30);

  @Test
  @DisplayName("고정 피드는 공개 시각이 오래되어도 고정되지 않은 피드보다 앞에 온다")
  void pinnedComesFirst() {
    HomeFeedSortKey pinnedOld = new HomeFeedSortKey(1L, true, OLD);
    HomeFeedSortKey unpinnedNew = new HomeFeedSortKey(2L, false, NEW);

    assertThat(unpinnedNew.isAfter(pinnedOld)).isTrue();
    assertThat(pinnedOld.isAfter(unpinnedNew)).isFalse();
  }

  @Test
  @DisplayName("공개 시각이 같으면 ID가 큰 피드가 앞에 온다")
  void sameScoreOrderedByIdDescending() {
    HomeFeedSortKey bigger = new HomeFeedSortKey(10L, false, NEW);
    HomeFeedSortKey smaller = new HomeFeedSortKey(9L, false, NEW);

    assertThat(smaller.isAfter(bigger)).isTrue();
    assertThat(bigger.isAfter(smaller)).isFalse();
  }

  @Test
  @DisplayName("커서로 바꿨다가 다시 읽으면 같은 위치다")
  void cursorRoundTrip() {
    HomeFeedSortKey sortKey = new HomeFeedSortKey(7L, true, OLD);

    HomeFeedSortKey parsed = HomeFeedSortKey.parse(sortKey.toCursor()).orElseThrow();

    assertThat(parsed).isEqualTo(sortKey);
  }

  @Test
  @DisplayName("커서가 없으면 빈 값이고, 모든 피드가 처음 위치 뒤에 온다")
  void emptyCursorIsStart() {
    assertThat(HomeFeedSortKey.parse(null)).isEmpty();
    assertThat(HomeFeedSortKey.parse(" ")).isEmpty();
    assertThat(new HomeFeedSortKey(1L, true, NEW).isAfter(HomeFeedSortKey.START)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "1_2_3", "-1_5", "100_0", "100_x", "9223372036854775807_1"})
  @DisplayName("형식이 틀린 커서는 거절한다")
  void rejectInvalidCursor(String cursor) {
    assertThatThrownBy(() -> HomeFeedSortKey.parse(cursor))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_CURSOR);
  }
}
