package kr.co.deepple.community.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.application.port.out.SelfIntroductionCommandRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.community.fixture.SelfIntroductionFixture;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class SelfIntroductionServiceTest {

  @Mock private SelfIntroductionCommandRepository selfIntroductionCommandRepository;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private S3Uploader s3Uploader;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private SelfIntroductionService selfIntroductionService;

  @Nested
  @DisplayName("셀프 소개 작성")
  class Write {

    @DisplayName("존재하지 않는 멤버의 ID인 경우, 예외 발생")
    @Test
    void throwExceptionWhenMemberNotFound() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() ->
              selfIntroductionService.write(
                  new SelfIntroductionWriteRequest(title, content, null), memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("셀프 소개를 작성한다.")
    @Test
    void writeSelfIntroduction() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));

      // When
      selfIntroductionService.write(
          new SelfIntroductionWriteRequest(title, content, null), memberId);

      // Then
      verify(selfIntroductionCommandRepository)
          .save(
              argThat(selfIntroduction ->
                  selfIntroduction.getMemberId() == memberId
                      && selfIntroduction.getContent().equals(content)
                      && selfIntroduction.getTitle().equals(title)
                      && selfIntroduction.getImageUrl() == null));
    }

    @DisplayName("이미지 URL을 포함하여 셀프 소개를 작성한다.")
    @Test
    void writeSelfIntroductionWithImage() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";
      String imageUrl = "https://example.com/image.jpg";

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));

      // When
      selfIntroductionService.write(
          new SelfIntroductionWriteRequest(title, content, imageUrl), memberId);

      // Then
      verify(selfIntroductionCommandRepository)
          .save(argThat(selfIntroduction -> imageUrl.equals(selfIntroduction.getImageUrl())));
    }

    @DisplayName("최초로 셀프 소개를 작성하는 경우, 셀프 소개 작성 완료 이벤트를 발행한다.")
    @Test
    void raiseEventWhenFirstSelfIntroduction() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.existsByMemberId(memberId)).thenReturn(false);

      // When
      boolean hasProcessedMission =
          selfIntroductionService.write(
              new SelfIntroductionWriteRequest(title, content, null), memberId);

      // Then
      Assertions.assertThat(hasProcessedMission).isTrue();
      Mockito.verify(eventPublisher)
          .publishEvent(
              ArgumentMatchers.<Object>argThat(event ->
                  event instanceof SelfIntroductionFirstWrittenEvent written
                      && written.memberId() == memberId));
    }

    @DisplayName("이미 셀프 소개를 작성한 적이 있는 경우, 셀프 소개 작성 완료 이벤트를 발행하지 않는다.")
    @Test
    void notRaiseEventWhenNotFirstSelfIntroduction() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.existsByMemberId(memberId)).thenReturn(true);

      // When
      boolean hasProcessedMission =
          selfIntroductionService.write(
              new SelfIntroductionWriteRequest(title, content, null), memberId);

      // Then
      Assertions.assertThat(hasProcessedMission).isFalse();
      Mockito.verifyNoInteractions(eventPublisher);
    }
  }

  @Nested
  @DisplayName("셀프 소개 업데이트")
  class Update {

    @Test
    @DisplayName("존재하지 않은 멤버의 ID인 경우, 예외 발생")
    void throwExceptionWhenMemberNotFound() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() ->
              selfIntroductionService.update(
                  new SelfIntroductionWriteRequest(title, content, null),
                  memberId,
                  selfIntroductionId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("해당 ID의 셀프 소개가 존재하지 않을 경우, 예외 발생")
    void throwExceptionWhenSelfIntroductionNotFound() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() ->
              selfIntroductionService.update(
                  new SelfIntroductionWriteRequest(title, content, null),
                  memberId,
                  selfIntroductionId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND);
    }

    @Test
    @DisplayName("수정하고자 하는 셀프 소개가 자신이 작성한 셀프 소개가 아닐 경우, 예외 발생")
    void throwExceptionWhenMemberIdFromSelfIntroductionIsNotEqualMemberId() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder()
              .id(selfIntroductionId)
              .memberId(1L)
              .title(title)
              .content(content)
              .build();

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When & Then
      Assertions.assertThatThrownBy(() ->
              selfIntroductionService.update(
                  new SelfIntroductionWriteRequest(title, content, null),
                  memberId,
                  selfIntroductionId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.SELF_INTRODUCTION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("셀프 소개를 업데이트한다.")
    void updateSelfIntroduction() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";
      String updatedContent = "셀프 소개 내용입니다. 최소 내용이 100자 이상입니다~!!! (100자 이상)";
      String newImageUrl = "https://example.com/image.jpg";

      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder()
              .id(selfIntroductionId)
              .memberId(memberId)
              .title(title)
              .content(content)
              .build();

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.update(
          new SelfIntroductionWriteRequest(title, updatedContent, newImageUrl),
          memberId,
          selfIntroductionId);

      // When
      Assertions.assertThat(selfIntroduction.getMemberId()).isEqualTo(memberId);
      Assertions.assertThat(selfIntroduction.getContent()).isEqualTo(updatedContent);
      Assertions.assertThat(selfIntroduction.getTitle()).isEqualTo(title);
      Assertions.assertThat(selfIntroduction.getImageUrl()).isEqualTo(newImageUrl);
    }
  }

  @Nested
  @DisplayName("셀프 소개 삭제")
  class Delete {

    @Test
    @DisplayName("존재하지 않은 멤버의 ID인 경우, 예외 발생")
    void throwExceptionWhenMemberNotFound() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;

      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(
              () -> selfIntroductionService.delete(selfIntroductionId, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("해당 ID의 셀프 소개가 존재하지 않을 경우, 예외 발생")
    void throwExceptionWhenSelfIntroductionNotFound() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(
              () -> selfIntroductionService.delete(selfIntroductionId, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND);
    }

    @Test
    @DisplayName("삭제하고자 하는 셀프 소개가 자신이 작성한 셀프 소개가 아닐 경우, 예외 발생")
    void throwExceptionWhenMemberIdFromSelfIntroductionIsNotEqualMemberId() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;

      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder().id(selfIntroductionId).memberId(1L).build();

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When & Then
      Assertions.assertThatThrownBy(
              () -> selfIntroductionService.delete(selfIntroductionId, memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.SELF_INTRODUCTION_ACCESS_DENIED);
    }

    @Test
    @DisplayName("셀프 소개를 삭제한다.")
    void deleteSelfIntroduction() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;

      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder().id(selfIntroductionId).memberId(memberId).build();

      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.delete(selfIntroductionId, memberId);

      // Then
      verify(selfIntroductionCommandRepository).deleteById(selfIntroductionId);
    }
  }

  @Nested
  @DisplayName("셀프 소개 공개 여부 변경")
  class UpdateSelfIntroduction {

    @Test
    @DisplayName("변경하고자 하는 셀프 소개가 존재하지 않는 경우, 예외 발생")
    void throwExceptionWhenSelfIntroductionNotFound() {
      // Given
      Long selfIntroductionId = 1L;
      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(
              () -> selfIntroductionService.changeOpenStatus(selfIntroductionId, false))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND);
    }

    @Test
    @DisplayName("셀프 소개를 공개로 변경한다.")
    void changeToOpen() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;
      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder()
              .id(selfIntroductionId)
              .memberId(memberId)
              .opened(false)
              .build();

      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.changeOpenStatus(selfIntroductionId, true);

      // Then
      Assertions.assertThat(selfIntroduction.isOpened()).isTrue();
    }

    @Test
    @DisplayName("셀프 소개를 비공개로 변경한다.")
    void changeToClose() {
      // Given
      Long selfIntroductionId = 1L;
      Long memberId = 2L;
      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder().id(selfIntroductionId).memberId(memberId).build();

      Mockito.when(selfIntroductionCommandRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.changeOpenStatus(selfIntroductionId, false);

      // Then
      Assertions.assertThat(selfIntroduction.isOpened()).isFalse();
    }
  }

  @Nested
  @DisplayName("셀프 소개 이미지 presigned URL 발급")
  class GetPresignedUrl {

    @Test
    @DisplayName("존재하지 않는 멤버의 ID인 경우, 예외 발생")
    void throwExceptionWhenMemberNotFound() {
      // Given
      Long memberId = 1L;
      Mockito.when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(
              () -> selfIntroductionService.getPresignedUrl("a.jpg", memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("self-introduction prefix로 presigned URL을 요청한다.")
    void getPresignedUrlWithPrefix() {
      // Given
      Long memberId = 1L;
      String fileName = "a.jpg";
      PresignedUrlResponse expected = new PresignedUrlResponse("https://put", "https://obj");
      Mockito.when(memberCommandRepository.findById(memberId))
          .thenReturn(Optional.of(MemberFixture.builder().id(memberId).build()));
      Mockito.when(s3Uploader.getPresignedUrl(eq(fileName), eq(memberId), eq("self-introduction")))
          .thenReturn(expected);

      // When
      PresignedUrlResponse actual = selfIntroductionService.getPresignedUrl(fileName, memberId);

      // Then
      Assertions.assertThat(actual).isEqualTo(expected);
      verify(s3Uploader).getPresignedUrl(any(), anyLong(), eq("self-introduction"));
    }
  }
}
