package kr.co.deepple.community.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.application.port.out.SelfIntroductionRepository;
import kr.co.deepple.community.domain.CommunityErrorCode;
import kr.co.deepple.community.domain.SelfIntroduction;
import kr.co.deepple.community.event.SelfIntroductionFirstWrittenEvent;
import kr.co.deepple.community.fixture.SelfIntroductionFixture;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.MemberSummaryFixture;
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

  @Mock private SelfIntroductionRepository selfIntroductionRepository;

  @Mock private FindMemberSummaryUseCase findMemberSummaryUseCase;

  @Mock private FindImageUseCase findImageUseCase;

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenThrow(new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND));

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));

      // When
      selfIntroductionService.write(
          new SelfIntroductionWriteRequest(title, content, null), memberId);

      // Then
      verify(selfIntroductionRepository)
          .save(
              argThat(selfIntroduction ->
                  selfIntroduction.getMemberId() == memberId
                      && selfIntroduction.getContent().equals(content)
                      && selfIntroduction.getTitle().equals(title)
                      && selfIntroduction.getImageId() == null
                      && selfIntroduction.getImageKey() == null));
    }

    @DisplayName("이미지를 포함하여 셀프 소개를 작성하면 이미지 key를 저장한다.")
    @Test
    void writeSelfIntroductionWithImage() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";
      long imageId = 10L;

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(findImageUseCase.getMemberImage(imageId, memberId))
          .thenReturn(new ImageInfo(imageId, "images/1/a.webp", "images/1/a_thumb.webp"));

      // When
      selfIntroductionService.write(
          new SelfIntroductionWriteRequest(title, content, imageId), memberId);

      // Then
      verify(selfIntroductionRepository)
          .save(
              argThat(selfIntroduction ->
                  Long.valueOf(imageId).equals(selfIntroduction.getImageId())
                      && "images/1/a.webp".equals(selfIntroduction.getImageKey())));
    }

    @DisplayName("내 이미지가 아니면 셀프 소개를 저장하지 않고 예외를 던진다.")
    @Test
    void throwExceptionWhenImageIsNotMine() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";
      long imageId = 10L;

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(findImageUseCase.getMemberImage(imageId, memberId))
          .thenThrow(new DeeppleException(ImageErrorCode.IMAGE_NOT_FOUND, "imageId=" + imageId));

      // When & Then
      Assertions.assertThatThrownBy(() ->
              selfIntroductionService.write(
                  new SelfIntroductionWriteRequest(title, content, imageId), memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", ImageErrorCode.IMAGE_NOT_FOUND);
      verify(selfIntroductionRepository, Mockito.never()).save(any());
    }

    @DisplayName("최초로 셀프 소개를 작성하는 경우, 셀프 소개 작성 완료 이벤트를 발행한다.")
    @Test
    void raiseEventWhenFirstSelfIntroduction() {
      // Given
      Long memberId = 1L;
      String title = "셀프 소개 제목";
      String content = "셀프 소개 내용입니다. 최소 내용이 30자 이상입니다~!!! (30자 이상)";

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.existsByMemberId(memberId)).thenReturn(false);

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.existsByMemberId(memberId)).thenReturn(true);

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenThrow(new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND));

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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
      long newImageId = 10L;

      SelfIntroduction selfIntroduction =
          SelfIntroductionFixture.builder()
              .id(selfIntroductionId)
              .memberId(memberId)
              .title(title)
              .content(content)
              .build();

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));
      Mockito.when(findImageUseCase.getMemberImage(newImageId, memberId))
          .thenReturn(new ImageInfo(newImageId, "images/2/a.webp", "images/2/a_thumb.webp"));

      // When
      selfIntroductionService.update(
          new SelfIntroductionWriteRequest(title, updatedContent, newImageId),
          memberId,
          selfIntroductionId);

      // When
      Assertions.assertThat(selfIntroduction.getMemberId()).isEqualTo(memberId);
      Assertions.assertThat(selfIntroduction.getContent()).isEqualTo(updatedContent);
      Assertions.assertThat(selfIntroduction.getTitle()).isEqualTo(title);
      Assertions.assertThat(selfIntroduction.getImageId()).isEqualTo(newImageId);
      Assertions.assertThat(selfIntroduction.getImageKey()).isEqualTo("images/2/a.webp");
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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenThrow(new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND));

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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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

      Mockito.when(findMemberSummaryUseCase.getMemberSummary(memberId))
          .thenReturn(MemberSummaryFixture.from(MemberFixture.builder().id(memberId).build()));
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.delete(selfIntroductionId, memberId);

      // Then
      verify(selfIntroductionRepository).deleteById(selfIntroductionId);
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
      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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

      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
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

      Mockito.when(selfIntroductionRepository.findById(selfIntroductionId))
          .thenReturn(Optional.of(selfIntroduction));

      // When
      selfIntroductionService.changeOpenStatus(selfIntroductionId, false);

      // Then
      Assertions.assertThat(selfIntroduction.isOpened()).isFalse();
    }
  }
}
