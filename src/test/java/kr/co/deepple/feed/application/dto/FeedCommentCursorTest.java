package kr.co.deepple.feed.application.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.domain.FeedErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("댓글 커서 테스트")
class FeedCommentCursorTest {

  @Test
  @DisplayName("커서로 바꿨다가 다시 읽으면 같은 댓글 ID다")
  void cursorRoundTrip() {
    FeedCommentCursor cursor = new FeedCommentCursor(30L);

    assertThat(FeedCommentCursor.parse(cursor.toCursor())).contains(cursor);
  }

  @Test
  @DisplayName("커서가 없으면 빈 값이다")
  void emptyCursor() {
    assertThat(FeedCommentCursor.parse(null)).isEmpty();
    assertThat(FeedCommentCursor.parse(" ")).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "0", "-1", "1_2", "9223372036854775808"})
  @DisplayName("형식이 틀린 커서는 거절한다")
  void rejectInvalidCursor(String cursor) {
    assertThatThrownBy(() -> FeedCommentCursor.parse(cursor))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(FeedErrorCode.INVALID_FEED_CURSOR);
  }
}
