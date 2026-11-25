package kr.co.deepple.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageContentType;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.image.domain.ImageStatus;
import kr.co.deepple.image.fixture.ImageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageQueryServiceTest {

  @Mock private ImageRepository imageRepository;

  @InjectMocks private ImageQueryService imageQueryService;

  @ParameterizedTest
  @EnumSource(
      value = ImageStatus.class,
      names = {"PROCESSING", "COMPLETED"})
  @DisplayName("처리 중이거나 완료된 내 이미지는 변환본 경로를 돌려준다.")
  void getMemberImage(ImageStatus status) {
    Image image = ImageFixture.builder().id(10L).memberId(1L).status(status).build();
    given(imageRepository.findById(10L)).willReturn(Optional.of(image));

    ImageInfo info = imageQueryService.getMemberImage(10L, 1L);

    assertThat(info.imageId()).isEqualTo(10L);
    assertThat(info.imageKey()).isEqualTo(image.getImageKey());
    assertThat(info.thumbnailKey()).isEqualTo(image.getThumbnailKey());
  }

  @Test
  @DisplayName("다른 회원의 이미지는 없는 이미지로 다룬다.")
  void rejectOtherMemberImage() {
    given(imageRepository.findById(10L))
        .willReturn(Optional.of(ImageFixture.builder().id(10L).memberId(2L).build()));

    assertThatThrownBy(() -> imageQueryService.getMemberImage(10L, 1L))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  @DisplayName("관리자가 올린 이미지는 회원 이미지로 쓸 수 없다.")
  void rejectAdminImage() {
    given(imageRepository.findById(10L))
        .willReturn(Optional.of(Image.uploadedByAdmin(1L, ImageContentType.WEBP)));

    assertThatThrownBy(() -> imageQueryService.getMemberImage(10L, 1L))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  @DisplayName("없는 이미지면 거절한다.")
  void rejectMissingImage() {
    given(imageRepository.findById(10L)).willReturn(Optional.empty());

    assertThatThrownBy(() -> imageQueryService.getMemberImage(10L, 1L))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  @DisplayName("처리에 실패한 이미지는 쓸 수 없다.")
  void rejectFailedImage() {
    given(imageRepository.findById(10L))
        .willReturn(
            Optional.of(
                ImageFixture.builder().id(10L).memberId(1L).status(ImageStatus.FAILED).build()));

    assertThatThrownBy(() -> imageQueryService.getMemberImage(10L, 1L))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_PROCESSING_FAILED);
  }

  @Test
  @DisplayName("관리자가 올린 이미지는 올린 관리자와 상관없이 쓸 수 있다.")
  void getAdminImage() {
    given(imageRepository.findById(10L))
        .willReturn(Optional.of(ImageFixture.builder().id(10L).adminId(2L).build()));

    assertThat(imageQueryService.getAdminImage(10L).imageId()).isEqualTo(10L);
  }

  @Test
  @DisplayName("회원이 올린 이미지는 관리자 이미지로 쓸 수 없다.")
  void rejectMemberImageAsAdminImage() {
    given(imageRepository.findById(10L))
        .willReturn(Optional.of(ImageFixture.builder().id(10L).memberId(1L).build()));

    assertThatThrownBy(() -> imageQueryService.getAdminImage(10L))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.IMAGE_NOT_FOUND);
  }

  @Test
  @DisplayName("변환이 끝난 이미지 ID만 돌려준다.")
  void findCompletedImageIds() {
    given(imageRepository.findIdsByIdInAndStatus(List.of(1L, 2L), ImageStatus.COMPLETED))
        .willReturn(List.of(2L));

    assertThat(imageQueryService.findCompletedImageIds(List.of(1L, 2L))).containsExactly(2L);
    assertThat(imageQueryService.findCompletedImageIds(List.of())).isEmpty();
  }
}
