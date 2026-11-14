package kr.co.deepple.like.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LikeTest {

  @Test
  @DisplayName("좋아요 생성")
  void createLike() {
    // Given
    long senderId = 1L;
    long receiverId = 2L;
    LikeLevel likeLevel = LikeLevel.INTERESTED;

    // When
    Like like = Like.of(senderId, receiverId, likeLevel);

    // Then
    Assertions.assertThat(like.getSenderId()).isEqualTo(senderId);
    Assertions.assertThat(like.getReceiverId()).isEqualTo(receiverId);
    Assertions.assertThat(like.getLevel()).isEqualTo(likeLevel);
  }

  @Nested
  @DisplayName("좋아요 생성 실패 케이스")
  class Fail {

    @Test
    @DisplayName("senderId가 null인 경우, 예외 발생")
    void throwExceptionWhenSenderIdIsNull() {
      // Given
      Long senderId = null;
      long receiverId = 1L;
      LikeLevel likeLevel = LikeLevel.INTERESTED;

      // When & Then
      Assertions.assertThatThrownBy(() -> Like.of(senderId, receiverId, likeLevel))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("receiverId가 null인 경우, 예외 발생")
    void throwExceptionWhenReceiverIdIsNull() {
      // Given
      long senderId = 1L;
      Long receiverId = null;
      LikeLevel likeLevel = LikeLevel.INTERESTED;

      // When & Then
      Assertions.assertThatThrownBy(() -> Like.of(senderId, receiverId, likeLevel))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("likeLevel이 null인 경우, 예외 발생")
    void throwExceptionWhenLikeLevelIsNull() {
      // Given
      long senderId = 1L;
      long receiverId = 2L;
      LikeLevel likeLevel = null;

      // When & Then
      Assertions.assertThatThrownBy(() -> Like.of(senderId, receiverId, likeLevel))
          .isInstanceOf(NullPointerException.class);
    }
  }
}
