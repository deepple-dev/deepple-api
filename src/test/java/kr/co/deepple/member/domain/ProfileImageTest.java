package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProfileImageTest {

  @Test
  @DisplayName("필드값에 NULL이 들어가지 않은 경우에는 성공.")
  void createWithMemberIdAndImageAndIsPrimaryAndOrder() {
    // Given
    Long memberId = 1L;
    long imageId = 10L;
    String imageKey = "images/1/a.webp";
    boolean isPrimary = true;
    int order = 1;

    // When
    ProfileImage profileImage =
        ProfileImage.builder()
            .memberId(memberId)
            .isPrimary(isPrimary)
            .order(order)
            .imageId(imageId)
            .imageKey(imageKey)
            .build();

    // Then
    Assertions.assertThat(profileImage).isNotNull();
  }

  @Test
  @DisplayName("order 값이 음수인 경우, 유효하지 않음")
  void isInvalidWhenOrderIsNegative() {
    // Given
    Long memberId = 1L;
    long imageId = 10L;
    String imageKey = "images/1/a.webp";
    boolean isPrimary = false;
    int order = -1;

    // When & Then
    Assertions.assertThatThrownBy(() ->
            ProfileImage.builder()
                .memberId(memberId)
                .imageId(imageId)
                .imageKey(imageKey)
                .isPrimary(isPrimary)
                .order(order)
                .build())
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_PROFILE_IMAGE_ORDER);
  }
}
