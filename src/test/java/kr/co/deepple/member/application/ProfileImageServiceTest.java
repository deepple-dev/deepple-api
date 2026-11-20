package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.member.application.dto.ProfileImageUploadRequest;
import kr.co.deepple.member.application.port.out.ProfileImageRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.event.MemberPrimaryProfileImageChangedEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class ProfileImageServiceTest {

  @InjectMocks private ProfileImageService profileImageService;

  @Mock private ProfileImageRepository profileImageRepository;

  @Mock private S3Uploader s3Uploader;

  @Mock private ApplicationEventPublisher eventPublisher;

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
      verify(profileImageRepository).deleteByMemberId(memberId);
      verify(profileImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("첫 번째 이미지를 대표 사진으로 담은 대표 사진 변경 이벤트를 발행한다.")
    void publishPrimaryProfileImageChangedEvent() {
      // Given
      long memberId = 1L;
      List<ProfileImageUploadRequest> requests =
          List.of(
              new ProfileImageUploadRequest("https://cdn.example.com/1.webp"),
              new ProfileImageUploadRequest("https://cdn.example.com/2.webp"));

      // When
      profileImageService.save(memberId, requests);

      // Then
      ArgumentCaptor<MemberPrimaryProfileImageChangedEvent> eventCaptor =
          ArgumentCaptor.forClass(MemberPrimaryProfileImageChangedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().memberId()).isEqualTo(memberId);
      assertThat(eventCaptor.getValue().imageUrl()).isEqualTo("https://cdn.example.com/1.webp");
    }

    @Test
    @DisplayName("이미지를 모두 지우면 대표 사진 없이 대표 사진 변경 이벤트를 발행한다.")
    void publishPrimaryProfileImageChangedEventWithoutImage() {
      // When
      profileImageService.save(1L, List.of());

      // Then
      ArgumentCaptor<MemberPrimaryProfileImageChangedEvent> eventCaptor =
          ArgumentCaptor.forClass(MemberPrimaryProfileImageChangedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().imageUrl()).isNull();
    }
  }
}
