package kr.co.deepple.match.command.domain.match.vo;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.match.command.domain.match.MatchErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MessageTest {

  @Test
  @DisplayName("메세지 값이 null인 경우 예외 반환")
  void throwsExceptionWhenMessageIsNull() {
    // Given
    String value = null;

    // When & Then
    Assertions.assertThatThrownBy(() -> Message.from(value))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("메세지 값이 빈 값인 경우 예외 반환")
  void throwsExceptionWhenMessageIsEmpty() {
    // Given
    String value = "";

    // When & Then
    Assertions.assertThatThrownBy(() -> Message.from(value))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.INVALID_MATCH_MESSAGE);
  }

  @Test
  @DisplayName("메세지가 200자를 넘으면 예외 반환")
  void throwsExceptionWhenMessageIsTooLong() {
    // Given
    String value = "가".repeat(201);

    // When & Then
    Assertions.assertThatThrownBy(() -> Message.from(value))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_MESSAGE_TOO_LONG);
  }

  @Test
  @DisplayName("이모지는 한 글자로 세어 200자까지 메세지 생성")
  void createMessageWithEmojiUpToMaxLength() {
    // Given
    String value = "😀".repeat(200);

    // When
    Message message = Message.from(value);

    // Then
    Assertions.assertThat(message.getValue()).isEqualTo(value);
  }

  @Test
  @DisplayName("메세지가 null 또는 빈 값이 아닌 경우, 메세지 생성")
  void createMessage() {
    // Given
    String value = "메세지입니다!";

    // When
    Message message = Message.from(value);

    // Then
    Assertions.assertThat(message.getValue()).isEqualTo(value);
  }
}
