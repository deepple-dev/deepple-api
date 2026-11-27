package kr.co.deepple.conversation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.conversation.domain.ConversationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConversationCursorTest {

  @Test
  @DisplayName("커서를 문자열로 바꿨다가 다시 읽으면 같은 값이다.")
  void roundTrip() {
    ConversationCursor cursor = ConversationCursor.of(LocalDateTime.of(2026, 11, 28, 1, 2, 3), 7L);

    assertThat(ConversationCursor.parse(cursor.toCursor())).contains(cursor);
  }

  @Test
  @DisplayName("최근 활동이 먼저, 같은 시각이면 회원 ID가 큰 쪽이 먼저다.")
  void orderByRecentActivity() {
    LocalDateTime time = LocalDateTime.of(2026, 11, 28, 1, 0);
    ConversationCursor recent = ConversationCursor.of(time.plusMinutes(1), 1L);
    ConversationCursor sameTimeBigId = ConversationCursor.of(time, 9L);
    ConversationCursor sameTimeSmallId = ConversationCursor.of(time, 3L);

    assertThat(recent.compareTo(sameTimeBigId)).isNegative();
    assertThat(sameTimeBigId.compareTo(sameTimeSmallId)).isNegative();
  }

  @ParameterizedTest
  @ValueSource(strings = {"abc", "1_2_3", "x_1"})
  @DisplayName("형식이 맞지 않는 커서는 거절한다.")
  void rejectInvalidCursor(String cursor) {
    assertThatThrownBy(() -> ConversationCursor.parse(cursor))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue(
            "errorCode", ConversationErrorCode.INVALID_CONVERSATION_CURSOR);
  }
}
