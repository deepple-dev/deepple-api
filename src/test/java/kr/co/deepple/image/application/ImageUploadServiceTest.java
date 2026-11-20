package kr.co.deepple.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.image.application.dto.ImageUploadRequest;
import kr.co.deepple.image.application.dto.ImageUploadResponse;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.application.port.out.PresignedUrlGenerator;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

  @Mock private ImageRepository imageRepository;

  @Mock private PresignedUrlGenerator presignedUrlGenerator;

  @InjectMocks private ImageUploadService imageUploadService;

  @Test
  @DisplayName("회원 이미지 기록을 처리 중으로 저장하고 그 임시 경로로 presigned URL을 준다.")
  void createMemberUpload() {
    given(imageRepository.save(any(Image.class)))
        .willAnswer(invocation -> {
          Image image = invocation.getArgument(0);
          ReflectionTestUtils.setField(image, "id", 10L);
          return image;
        });
    given(presignedUrlGenerator.generate(startsWith("tmp/members/1/"), eq("image/webp"), eq(1024L)))
        .willReturn("https://upload");

    ImageUploadResponse response =
        imageUploadService.createMemberUpload(1L, new ImageUploadRequest("image/webp", 1024L));

    assertThat(response.imageId()).isEqualTo(10L);
    assertThat(response.uploadUrl()).isEqualTo("https://upload");
  }

  @Test
  @DisplayName("관리자 이미지는 관리자 임시 경로로 presigned URL을 준다.")
  void createAdminUpload() {
    given(imageRepository.save(any(Image.class)))
        .willAnswer(invocation -> {
          Image image = invocation.getArgument(0);
          ReflectionTestUtils.setField(image, "id", 11L);
          return image;
        });
    given(presignedUrlGenerator.generate(startsWith("tmp/admins/2/"), eq("image/png"), eq(2048L)))
        .willReturn("https://upload");

    ImageUploadResponse response =
        imageUploadService.createAdminUpload(2L, new ImageUploadRequest("image/png", 2048L));

    assertThat(response.imageId()).isEqualTo(11L);
    assertThat(response.uploadUrl()).isEqualTo("https://upload");
  }

  @Test
  @DisplayName("지원하지 않는 형식이면 기록을 만들지 않는다.")
  void rejectUnsupportedContentType() {
    assertThatThrownBy(() ->
            imageUploadService.createMemberUpload(1L, new ImageUploadRequest("image/heic", 1024L)))
        .isInstanceOf(DeeppleException.class)
        .extracting("errorCode")
        .isEqualTo(ImageErrorCode.UNSUPPORTED_IMAGE_CONTENT_TYPE);
    verifyNoInteractions(imageRepository, presignedUrlGenerator);
  }
}
