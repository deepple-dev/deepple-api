package kr.co.deepple.member.domain;

import kr.co.deepple.common.exception.DeeppleException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProfileImageTest {

  @Test
  @DisplayName("필드값에 NULL이 들어가지 않은 경우에는 성공.")
  void createWithMemberIdAndImageUrlAndIsPrimaryAndOrder() {
    // Given
    Long memberId = 1L;
    String imageUrl = "url";
    boolean isPrimary = true;
    int order = 1;

    // When
    ProfileImage profileImage =
        ProfileImage.builder()
            .memberId(memberId)
            .isPrimary(isPrimary)
            .order(order)
            .imageUrl(ImageUrl.from(imageUrl))
            .build();

    // Then
    Assertions.assertThat(profileImage).isNotNull();
  }

  @Test
  @DisplayName("멤버 아이디의 값이 NULL인 경우, 유효하지 않음")
  void isInvalidWhenMemberIdIsNegative() {
    // Given
    Long memberId = null;
    String imageUrl = "url";
    boolean isPrimary = false;
    int order = -1;

    // When & Then
    Assertions.assertThatThrownBy(() ->
            ProfileImage.builder()
                .memberId(memberId)
                .imageUrl(ImageUrl.from(imageUrl))
                .isPrimary(isPrimary)
                .order(order)
                .build())
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("order 값이 음수인 경우, 유효하지 않음")
  void isInvalidWhenOrderIsNegative() {
    // Given
    Long memberId = 1L;
    String imageUrl = "url";
    boolean isPrimary = false;
    int order = -1;

    // When & Then
    Assertions.assertThatThrownBy(() ->
            ProfileImage.builder()
                .memberId(memberId)
                .imageUrl(ImageUrl.from(imageUrl))
                .isPrimary(isPrimary)
                .order(order)
                .build())
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_PROFILE_IMAGE_ORDER);
  }
}
