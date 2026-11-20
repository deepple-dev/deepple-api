package kr.co.deepple.image.domain;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.deepple.image.fixture.ImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ImageTest {

  @Test
  @DisplayName("회원이 올리는 이미지는 회원별 임시 경로와 변환본 경로를 같은 이름으로 갖는다.")
  void uploadedByMember() {
    Image image = Image.uploadedByMember(7L, ImageContentType.JPEG);

    String name = image.getSourceKey().substring("tmp/members/7/".length());
    assertThat(image.getStatus()).isEqualTo(ImageStatus.PROCESSING);
    assertThat(image.getSourceKey()).startsWith("tmp/members/7/");
    assertThat(image.getImageKey()).isEqualTo("images/members/7/" + name + ".webp");
    assertThat(image.getThumbnailKey()).isEqualTo("images/members/7/" + name + "-thumbnail.webp");
  }

  @Test
  @DisplayName("관리자가 올리는 이미지는 관리자 경로를 쓴다.")
  void uploadedByAdmin() {
    Image image = Image.uploadedByAdmin(3L, ImageContentType.PNG);

    assertThat(image.getSourceKey()).startsWith("tmp/admins/3/");
    assertThat(image.isOwnedBy(ImageOwnerType.ADMIN, 3L)).isTrue();
    assertThat(image.isOwnedBy(ImageOwnerType.MEMBER, 3L)).isFalse();
  }

  @Nested
  @DisplayName("처리 결과 반영")
  class ApplyResult {

    @Test
    @DisplayName("처리 중인 이미지를 완료로 바꾼다.")
    void complete() {
      Image image = ImageFixture.builder().build();

      image.complete();

      assertThat(image.getStatus()).isEqualTo(ImageStatus.COMPLETED);
    }

    @Test
    @DisplayName("처리 중인 이미지를 실패로 바꾸고 이유를 남긴다.")
    void fail() {
      Image image = ImageFixture.builder().build();

      image.fail("이미지가 아닙니다");

      assertThat(image.getStatus()).isEqualTo(ImageStatus.FAILED);
      assertThat(image.getFailureReason()).isEqualTo("이미지가 아닙니다");
    }

    @Test
    @DisplayName("실패 이유가 컬럼 길이보다 길면 255자까지만 남긴다.")
    void truncateLongFailureReason() {
      Image image = ImageFixture.builder().build();

      image.fail("가".repeat(300));

      assertThat(image.getFailureReason()).hasSize(255);
    }

    @Test
    @DisplayName("이미 끝난 이미지에 다른 결과가 다시 와도 바꾸지 않는다.")
    void ignoreAfterFinished() {
      Image image = ImageFixture.builder().status(ImageStatus.COMPLETED).build();

      image.fail("이미지가 아닙니다");

      assertThat(image.getStatus()).isEqualTo(ImageStatus.COMPLETED);
      assertThat(image.getFailureReason()).isNull();
    }
  }
}
