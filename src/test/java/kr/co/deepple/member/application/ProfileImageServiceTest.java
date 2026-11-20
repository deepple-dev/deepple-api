package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.stream.LongStream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.member.application.dto.ProfileImageUploadRequest;
import kr.co.deepple.member.application.dto.ProfileImageUploadResponse;
import kr.co.deepple.member.application.port.out.ProfileImageRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.event.MemberPrimaryProfileImageChangedEvent;
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

  @Mock private FindImageUseCase findImageUseCase;

  @Mock private S3Uploader s3Uploader;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Nested
  @DisplayName("프로필 이미지 엔티티 저장 테스트")
  class SaveProfileImage {

    @Test
    @DisplayName("프로필 이미지 저장 요청 개수가 6개를 초과하는 경우, 예외 발생.")
    void throwExceptionWhenRequestSizeIsOver6() {
      // Given
      long memberId = 1L;
      List<ProfileImageUploadRequest> requests =
          LongStream.rangeClosed(1L, 7L).mapToObj(ProfileImageUploadRequest::new).toList();

      // When & Then
      assertThatThrownBy(() -> profileImageService.save(memberId, requests))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
    }

    @Test
    @DisplayName("같은 이미지를 여러 번 넣으면 기존 사진을 지우지 않고 거절한다.")
    void rejectDuplicateImage() {
      long memberId = 1L;
      List<ProfileImageUploadRequest> requests =
          List.of(new ProfileImageUploadRequest(10L), new ProfileImageUploadRequest(10L));

      assertThatThrownBy(() -> profileImageService.save(memberId, requests))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.DUPLICATE_PROFILE_IMAGE);
      verifyNoInteractions(profileImageRepository, findImageUseCase);
    }

    @Test
    @DisplayName("요청한 이미지의 key를 요청 순서대로 저장하고 첫 번째를 대표 사진으로 둔다.")
    void saveImageKeysInRequestOrder() {
      // Given
      long memberId = 1L;
      given(findImageUseCase.getMemberImage(10L, memberId))
          .willReturn(new ImageInfo(10L, "images/1/a.webp", "images/1/a_thumb.webp"));
      given(findImageUseCase.getMemberImage(11L, memberId))
          .willReturn(new ImageInfo(11L, "images/1/b.webp", "images/1/b_thumb.webp"));
      given(s3Uploader.getObjectUrl(any()))
          .willAnswer(invocation -> "https://cdn.example.com/" + invocation.getArgument(0));

      // When
      List<ProfileImageUploadResponse> responses =
          profileImageService.save(
              memberId,
              List.of(new ProfileImageUploadRequest(10L), new ProfileImageUploadRequest(11L)));

      // Then
      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<ProfileImage>> captor = ArgumentCaptor.forClass(List.class);
      verify(profileImageRepository).deleteByMemberId(memberId);
      verify(profileImageRepository).saveAll(captor.capture());
      assertThat(captor.getValue())
          .extracting(
              ProfileImage::getImageId,
              ProfileImage::getImageKey,
              ProfileImage::getOrder,
              ProfileImage::isPrimary)
          .containsExactly(
              tuple(10L, "images/1/a.webp", 1, true), tuple(11L, "images/1/b.webp", 2, false));
      assertThat(responses)
          .extracting(ProfileImageUploadResponse::imageUrl)
          .containsExactly(
              "https://cdn.example.com/images/1/a.webp", "https://cdn.example.com/images/1/b.webp");
    }

    @Test
    @DisplayName("내 이미지가 아니면 기존 프로필 이미지를 지우지 않고 예외를 던진다.")
    void throwExceptionWhenImageIsNotMine() {
      // Given
      long memberId = 1L;
      given(findImageUseCase.getMemberImage(10L, memberId))
          .willThrow(new DeeppleException(ImageErrorCode.IMAGE_NOT_FOUND, "imageId=10"));

      // When & Then
      assertThatThrownBy(
              () -> profileImageService.save(memberId, List.of(new ProfileImageUploadRequest(10L))))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ImageErrorCode.IMAGE_NOT_FOUND);
      verify(profileImageRepository, never()).deleteByMemberId(memberId);
      verify(profileImageRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("첫 번째 이미지의 URL을 대표 사진으로 담은 대표 사진 변경 이벤트를 발행한다.")
    void publishPrimaryProfileImageChangedEvent() {
      // Given
      long memberId = 1L;
      given(findImageUseCase.getMemberImage(10L, memberId))
          .willReturn(new ImageInfo(10L, "images/1/a.webp", "images/1/a_thumb.webp"));
      given(findImageUseCase.getMemberImage(11L, memberId))
          .willReturn(new ImageInfo(11L, "images/1/b.webp", "images/1/b_thumb.webp"));
      given(s3Uploader.getObjectUrl(any()))
          .willAnswer(invocation -> "https://cdn.example.com/" + invocation.getArgument(0));

      // When
      profileImageService.save(
          memberId,
          List.of(new ProfileImageUploadRequest(10L), new ProfileImageUploadRequest(11L)));

      // Then
      ArgumentCaptor<MemberPrimaryProfileImageChangedEvent> eventCaptor =
          ArgumentCaptor.forClass(MemberPrimaryProfileImageChangedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().memberId()).isEqualTo(memberId);
      assertThat(eventCaptor.getValue().imageUrl())
          .isEqualTo("https://cdn.example.com/images/1/a.webp");
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
