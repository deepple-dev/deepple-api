package kr.co.deepple.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageStatus;
import kr.co.deepple.image.fixture.ImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageProcessingResultServiceTest {

  @Mock private ImageRepository imageRepository;

  @InjectMocks private ImageProcessingResultService imageProcessingResultService;

  @Test
  @DisplayName("완료 결과를 받으면 이미지를 완료로 바꾼다.")
  void complete() {
    Image image = ImageFixture.builder().build();
    given(imageRepository.findBySourceKeyForUpdate(image.getSourceKey()))
        .willReturn(Optional.of(image));

    imageProcessingResultService.complete(image.getSourceKey());

    assertThat(image.getStatus()).isEqualTo(ImageStatus.COMPLETED);
  }

  @Test
  @DisplayName("실패 결과를 받으면 이미지를 실패로 바꾼다.")
  void fail() {
    Image image = ImageFixture.builder().build();
    given(imageRepository.findBySourceKeyForUpdate(image.getSourceKey()))
        .willReturn(Optional.of(image));

    imageProcessingResultService.fail(image.getSourceKey(), "이미지가 아닙니다");

    assertThat(image.getStatus()).isEqualTo(ImageStatus.FAILED);
    assertThat(image.getFailureReason()).isEqualTo("이미지가 아닙니다");
  }

  @Test
  @DisplayName("해당하는 기록이 없으면 넘어간다.")
  void ignoreUnknownSourceKey() {
    given(imageRepository.findBySourceKeyForUpdate("tmp/members/1/unknown"))
        .willReturn(Optional.empty());

    assertThatCode(() -> imageProcessingResultService.complete("tmp/members/1/unknown"))
        .doesNotThrowAnyException();
  }
}
