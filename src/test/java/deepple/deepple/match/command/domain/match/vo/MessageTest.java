package deepple.deepple.match.command.domain.match.vo;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.match.command.domain.match.MatchErrorCode;
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
