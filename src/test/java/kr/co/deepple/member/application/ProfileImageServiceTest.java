package kr.co.deepple.member.application;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.member.application.dto.ProfileImageUploadRequest;
import kr.co.deepple.member.application.port.out.ProfileImageCommandRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceTest {

  @InjectMocks private ProfileImageService profileImageService;

  @Mock private ProfileImageCommandRepository profileImageCommandRepository;

  @Mock private S3Uploader s3Uploader;

  @Nested
  @DisplayName("pre-signed URL 테스트")
  class GetPresignedUrl {

    @Test
    @DisplayName("이미지 확장자가 허용되지 않는 경우, 예외 발생")
    void throwExceptionWhenFileExtensionIsNotIncluded_invalidExtension() {
      // Given
      String fileName = "error.txt";
      Long userId = 1L;

      // When & Then
      Assertions.assertThatThrownBy(() -> profileImageService.getPresignedUrl(fileName, userId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", MemberErrorCode.INVALID_PROFILE_IMAGE_EXTENSION);
    }
  }

  @Nested
  @DisplayName("프로필 이미지 엔티티 저장 테스트")
  class saveProfileImage {

    @Test
    @DisplayName("프로필 이미지 저장 요청 개수가 6개를 초과하는 경우, 예외 발생.")
    void throwExceptionWhenRequestSizeIsOver6() {
      // Given
      Long memberId = 1L;

      List<ProfileImageUploadRequest> requests =
          List.of(
              new ProfileImageUploadRequest("url1"),
              new ProfileImageUploadRequest("url2"),
              new ProfileImageUploadRequest("url3"),
              new ProfileImageUploadRequest("url4"),
              new ProfileImageUploadRequest("url5"),
              new ProfileImageUploadRequest("url6"),
              new ProfileImageUploadRequest("url7"));

      // When & Then
      Assertions.assertThatThrownBy(() -> profileImageService.save(memberId, requests))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
    }

    void uploadImage() {
      // Given
      Long memberId = 1L;
      List<ProfileImageUploadRequest> request =
          List.of(
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_1.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_2.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_3.jpeg"),
              new ProfileImageUploadRequest(
                  "https://bucket.s3.region.amazonaws.com/TEMP_IMAGE_4.jpeg"));
      // When
      profileImageService.save(memberId, request);

      // Then
      verify(profileImageCommandRepository).deleteByMemberId(memberId);
      verify(profileImageCommandRepository).saveAll(anyList());
    }
  }
}
