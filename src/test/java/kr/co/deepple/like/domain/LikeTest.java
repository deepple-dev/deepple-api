package kr.co.deepple.like.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
}
