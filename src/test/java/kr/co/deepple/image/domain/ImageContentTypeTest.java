package kr.co.deepple.image.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.common.exception.DeeppleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImageContentTypeTest {

  @Test
  @DisplayName("MIME 타입으로 이미지 형식을 찾는다.")
  void from() {
    assertThat(ImageContentType.from("image/webp")).isEqualTo(ImageContentType.WEBP);
  }

  @ParameterizedTest
  @ValueSource(strings = {"image/heic", "image/gif", "application/pdf", "IMAGE/JPEG"})
  @DisplayName("JPEG, PNG, WebP가 아니면 거절한다.")
  void rejectUnsupported(String mimeType) {
    assertThatThrownBy(() -> ImageContentType.from(mimeType))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.UNSUPPORTED_IMAGE_CONTENT_TYPE);
  }
}
