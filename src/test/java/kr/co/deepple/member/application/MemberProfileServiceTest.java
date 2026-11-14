package kr.co.deepple.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.dto.MemberProfileUpdateRequest;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberProfileService memberProfileService;

  @DisplayName("멤버를 찾을 수 없을 경우, 예외 발생")
  @Test
  void throwExceptionWhenMemberIsNotFound() {
    // Given
    Long memberId = 1L;

    // When
    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

    // Then
    assertThatThrownBy(() -> memberProfileService.updateMember(memberId, null))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
  }

  @DisplayName("멤버가 존재하지만 Enum 값이 적절하지 않은 경우, 예외 발생")
  @Test
  void throwExceptionWhenEnumValueIsInvalid() {
    // Given
    Long memberId = 1L;
    Job job = Job.JOB_SEARCHING;
    Set<Hobby> hobbies = Set.of(Hobby.ANIMATION, Hobby.DANCE);

    MemberProfileUpdateRequest invalidRequest =
        new MemberProfileUpdateRequest(
            "nickname",
            "INVALID_ENUM",
            20,
            180, // 잘못된 gender 값
            "Daejeon",
            "OTHER",
            "ENFJ",
            "DAILY",
            "QUIT",
            "BUDDHIST",
            hobbies.stream().map(Enum::name).collect(Collectors.toSet()),
            job.name());
    Member existingMember =
        MemberFixture.builder().activityStatus(ActivityStatus.INITIAL).noProfile().build();

    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When & Then
    assertThatThrownBy(() -> memberProfileService.updateMember(memberId, invalidRequest))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_PROFILE_OPTION);
  }

  @DisplayName("멤버가 존재하는 경우, 성공")
  @Test
  void succeedsWhenMemberIsFound() {
    /** TODO : 수정. */
    // Given
    Long memberId = 1L;
    Job job = Job.JOB_SEARCHING;
    Set<Hobby> hobbies = Set.of(Hobby.ANIMATION, Hobby.DANCE);

    MemberProfileUpdateRequest request =
        new MemberProfileUpdateRequest(
            "nickname",
            "MALE",
            Calendar.getInstance().get(Calendar.YEAR) - 19,
            180,
            "DONG_GU_DAEJEON",
            "OTHER",
            "ENFJ",
            "DAILY",
            "QUIT",
            "BUDDHIST",
            hobbies.stream().map(Enum::name).collect(Collectors.toSet()),
            job.name());
    Member existingMember =
        MemberFixture.builder()
            .id(memberId)
            .activityStatus(ActivityStatus.INITIAL)
            .noProfile()
            .build();

    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When
    memberProfileService.updateMember(memberId, request);

    // Then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSummaryChangedEvent.class));
    assertThat(existingMember.getProfile().getNickname().getValue()).isEqualTo("nickname");
    assertThat(existingMember.getProfile().getGender()).isEqualTo(Gender.MALE);
    assertThat(existingMember.getProfile().getJob()).isEqualTo(job);
    assertThat(existingMember.getProfile().getHobbies()).hasSameSizeAs(hobbies);
    assertThat(existingMember.getProfile().getHeight()).isEqualTo(180);
    assertThat(existingMember.getProfile().getRegion().getCity()).isEqualTo(City.DAEJEON);
    assertThat(existingMember.getProfile().getReligion()).isEqualTo(Religion.BUDDHIST);
  }

  @DisplayName("멤버가 존재하는 경우, 특정 값에 null 이 포함되어 있더라도 성공.")
  @Test
  void succeedsWhenNullValueExists() {
    // Given
    Long memberId = 1L;
    Job job = Job.JOB_SEARCHING;

    MemberProfileUpdateRequest request =
        new MemberProfileUpdateRequest(
            "nickname",
            "MALE",
            Calendar.getInstance().get(Calendar.YEAR) - 19,
            180,
            null,
            "OTHER",
            "ENFJ",
            "DAILY",
            "QUIT",
            "BUDDHIST",
            null,
            job.name());
    Member existingMember =
        MemberFixture.builder()
            .id(memberId)
            .activityStatus(ActivityStatus.INITIAL)
            .noProfile()
            .build();

    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When
    memberProfileService.updateMember(memberId, request);

    // Then
    assertThat(existingMember.getProfile().getNickname().getValue()).isEqualTo("nickname");
    assertThat(existingMember.getProfile().getGender()).isEqualTo(Gender.MALE);
    assertThat(existingMember.getProfile().getJob()).isEqualTo(job);
    assertThat(existingMember.getProfile().getHobbies()).isNull();
    assertThat(existingMember.getProfile().getHeight()).isEqualTo(180);
    assertThat(existingMember.getProfile().getRegion()).isNull();
    assertThat(existingMember.getProfile().getReligion()).isEqualTo(Religion.BUDDHIST);
  }

  @Nested
  @DisplayName("changeProfilePublishStatus 메서드 테스트")
  class ChangeProfilePublishStatusMethodTest {

    @Test
    @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
    void throwsExceptionWhenMemberNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When
      assertThatThrownBy(() -> memberProfileService.changeProfilePublishStatus(memberId, true))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("멤버가 존재하면 member.publishProfile() 메서드를 호출합니다.")
    void callsPublishProfileMethodWhenMemberExists() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).profilePublic(false).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeProfilePublishStatus(memberId, true);

      // Then
      assertThat(member.isProfilePublic()).isTrue();
    }
  }

  @Nested
  @DisplayName("changeMemberActivityStatus 메서드 테스트")
  class ChangeMemberActivityStatusMethodTest {

    @Test
    @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
    void throwsExceptionWhenMemberNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When
      assertThatThrownBy(
              () -> memberProfileService.changeMemberActivityStatus(memberId, "TEMPORARY"))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원의 상태를 활동중이 아닌 다른 상태로 변경하는 경우, 프로필 공개 상태 또한 비공개로 변경합니다.")
    void callsChangeActivityStatusAndNonPublishProfileWhenActivityStatusIsNotActive() {
      // Given
      long memberId = 1L;
      String activityStatus = "TEMPORARY";
      Member member = MemberFixture.builder().id(memberId).profilePublic(true).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeMemberActivityStatus(memberId, activityStatus);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.SUSPENDED_TEMPORARILY);
      assertThat(member.isProfilePublic()).isFalse();
    }
  }

  @Nested
  @DisplayName("validateContactTypeSetting 메소드 테스트.")
  class ValidateContactTypeSettingTest {

    @Test
    @DisplayName("멤버가 존재하지 않으면, 예외를 던집니다.")
    void throwExceptionWhenMemberNotFound() {
      // Given
      Long memberId = 1L;
      String contactType = "PHONE_NUMBER";
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () -> memberProfileService.validateContactTypeSetting(memberId, contactType))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("ContactType이 Null 일 경우, 유효성을 검증하지 않는다.")
    void shouldNotValidateWhenContactTypeIsNull() {
      // Given
      Long memberId = 1L;
      String contactType = null;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.validateContactTypeSetting(memberId, contactType);

      // Then
      assertThatNoException();
    }

    @Test
    @DisplayName("ContactType 이 PHONE_NUMBER or KAKAO 일 경우, 유효성을 검증한다.")
    void shouldValidateWhenContactTypeIsInKakaoOrPhonNumber() {
      // Given
      Long memberId = 1L;
      List<String> contactTypes = List.of("PHONE_NUMBER", "KAKAO");
      Member member = Mockito.mock(Member.class);
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
      when(member.getPhoneNumber()).thenReturn(null);
      when(member.getKakaoId()).thenReturn(null);

      // When & Then
      for (String contactType : contactTypes) {
        assertThatThrownBy(
                () -> memberProfileService.validateContactTypeSetting(memberId, contactType))
            .isInstanceOf(DeeppleException.class)
            .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.CONTACT_TYPE_SETTING_NEEDED);
      }
    }
  }

  @Nested
  @DisplayName("활동 상태 변경 이벤트 테스트")
  class ActivityStatusEventTest {

    @Test
    @DisplayName("휴면으로 전환하면 휴면 전환 이벤트를 발행합니다.")
    void publishDormantEventWhenDeactivated() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.ACTIVE).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.deactivate(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.DORMANT);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameDormantEvent.class));
    }

    @Test
    @DisplayName("휴면을 해제하면 활동 전환 이벤트를 발행합니다.")
    void publishActiveEventWhenReactivated() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.DORMANT).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.reactivate(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameActiveEvent.class));
    }

    @Test
    @DisplayName("심사가 승인되면 활동 전환 이벤트를 발행합니다.")
    void publishActiveEventWhenScreeningApproved() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.INITIAL).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.approveScreening(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameActiveEvent.class));
    }
  }

  @Nested
  @DisplayName("심사 상태 변경 테스트")
  class ChangeScreeningStatusTest {

    @Test
    @DisplayName("심사가 반려되면 심사 거절 상태로 바꾸고 프로필을 비공개로 변경합니다.")
    void changeToRejectedAndNonPublishProfile() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).profilePublic(true).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeReject(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.REJECTED_SCREENING);
      assertThat(member.isProfilePublic()).isFalse();
    }

    @Test
    @DisplayName("심사가 생성되면 심사 대기 상태로 바꾸고 프로필을 비공개로 변경합니다.")
    void changeToWaitingAndNonPublishProfile() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).profilePublic(true).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeWaiting(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.WAITING_SCREENING);
      assertThat(member.isProfilePublic()).isFalse();
    }

    @Test
    @DisplayName("심사가 승인되면 프로필을 공개로 변경합니다.")
    void publishProfileWhenScreeningApproved() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder()
              .id(memberId)
              .activityStatus(ActivityStatus.WAITING_SCREENING)
              .profilePublic(false)
              .build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.approveScreening(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
      assertThat(member.isProfilePublic()).isTrue();
    }

    @Test
    @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
    void throwsExceptionWhenMemberNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> memberProfileService.changeWaiting(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("markDatingExamSubmitted 메서드 테스트")
  class MarkDatingExamSubmittedTest {

    @Test
    @DisplayName("연애고사 제출 여부를 표시하고, 다시 호출해도 결과가 같습니다.")
    void markDatingExamSubmitted() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.markDatingExamSubmitted(memberId);
      memberProfileService.markDatingExamSubmitted(memberId);

      // Then
      assertThat(member.hasSubmittedDatingExam()).isTrue();
    }

    @Test
    @DisplayName("멤버가 존재하지 않으면 예외를 던집니다.")
    void throwsExceptionWhenMemberNotFound() {
      // Given
      long memberId = 1L;
      when(memberCommandRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> memberProfileService.markDatingExamSubmitted(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }
}
