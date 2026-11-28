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
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.event.MemberActivityStatusChangedEvent;
import kr.co.deepple.member.event.MemberBecameActiveEvent;
import kr.co.deepple.member.event.MemberBecameDormantEvent;
import kr.co.deepple.member.event.MemberProfileChangedEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceTest {

  @Mock private MemberRepository memberRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberProfileService memberProfileService;

  @DisplayName("멤버를 찾을 수 없을 경우, 예외 발생")
  @Test
  void throwExceptionWhenMemberIsNotFound() {
    // Given
    Long memberId = 1L;

    // When
    when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

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

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

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

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When
    memberProfileService.updateMember(memberId, request);

    // Then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberSummaryChangedEvent.class));
    ArgumentCaptor<MemberProfileChangedEvent> profileEventCaptor =
        ArgumentCaptor.forClass(MemberProfileChangedEvent.class);
    Mockito.verify(eventPublisher).publishEvent(profileEventCaptor.capture());
    MemberProfileChangedEvent profileEvent = profileEventCaptor.getValue();
    assertThat(profileEvent.nickname()).isEqualTo("nickname");
    assertThat(profileEvent.gender()).isEqualTo("MALE");
    assertThat(profileEvent.yearOfBirth()).isEqualTo(request.yearOfBirth());
    assertThat(profileEvent.city()).isEqualTo("DAEJEON");
    assertThat(profileEvent.mbti()).isEqualTo("ENFJ");
    assertThat(existingMember.getProfile().getNickname().getValue()).isEqualTo("nickname");
    assertThat(existingMember.getProfile().getGender()).isEqualTo(Gender.MALE);
    assertThat(existingMember.getProfile().getJob()).isEqualTo(job);
    assertThat(existingMember.getProfile().getHobbies()).hasSameSizeAs(hobbies);
    assertThat(existingMember.getProfile().getHeight()).isEqualTo(180);
    assertThat(existingMember.getProfile().getRegion().getCity()).isEqualTo(City.DAEJEON);
    assertThat(existingMember.getProfile().getReligion()).isEqualTo(Religion.BUDDHIST);
  }

  @DisplayName("가입 뒤 47세가 된 회원도 태어난 해를 그대로 두면 다른 항목을 수정할 수 있다")
  @Test
  void updatesProfileWhenStoredYearOfBirthIsUnchangedButOutOfRange() {
    // Given
    long memberId = 1L;
    int yearOfBirthAge47 = Calendar.getInstance().get(Calendar.YEAR) - 46;
    Member existingMember =
        MemberFixture.builder().id(memberId).yearOfBirth(yearOfBirthAge47).build();
    MemberProfileUpdateRequest request = updateRequest(yearOfBirthAge47, Set.of("TRAVEL"));
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When
    memberProfileService.updateMember(memberId, request);

    // Then
    assertThat(existingMember.getProfile().getYearOfBirth().getValue()).isEqualTo(yearOfBirthAge47);
    assertThat(existingMember.getProfile().getHobbies()).containsExactly(Hobby.TRAVEL);
  }

  @DisplayName("태어난 해를 범위 밖의 다른 값으로 바꾸면 예외 발생")
  @Test
  void throwsWhenYearOfBirthChangesToOutOfRange() {
    // Given
    long memberId = 1L;
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    Member existingMember =
        MemberFixture.builder().id(memberId).yearOfBirth(currentYear - 46).build();
    MemberProfileUpdateRequest request = updateRequest(currentYear - 47, Set.of("TRAVEL"));
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When & Then
    assertThatThrownBy(() -> memberProfileService.updateMember(memberId, request))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_YEAR_OF_BIRTH);
  }

  @DisplayName("처음 프로필을 쓸 때 범위 밖 태어난 해면 예외 발생")
  @Test
  void throwsWhenNewProfileYearOfBirthIsOutOfRange() {
    // Given
    long memberId = 1L;
    Member existingMember = MemberFixture.builder().id(memberId).noProfile().build();
    MemberProfileUpdateRequest request =
        updateRequest(Calendar.getInstance().get(Calendar.YEAR) - 46, Set.of("TRAVEL"));
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

    // When & Then
    assertThatThrownBy(() -> memberProfileService.updateMember(memberId, request))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_YEAR_OF_BIRTH);
  }

  private MemberProfileUpdateRequest updateRequest(int yearOfBirth, Set<String> hobbies) {
    return new MemberProfileUpdateRequest(
        "nickname",
        "MALE",
        yearOfBirth,
        180,
        "DONG_GU_DAEJEON",
        "OTHER",
        "ENFJ",
        "DAILY",
        "QUIT",
        "BUDDHIST",
        hobbies,
        Job.JOB_SEARCHING.name());
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

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.deactivate(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.DORMANT);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameDormantEvent.class));
      verifyActivityStatusChangedEvent(memberId, "DORMANT");
    }

    @ParameterizedTest
    @EnumSource(
        value = ActivityStatus.class,
        names = {"ACTIVE", "SUSPENDED_TEMPORARILY", "SUSPENDED_PERMANENTLY"})
    @DisplayName("관리자가 활동 상태를 바꾸면 바뀐 상태를 담은 활동 상태 변경 이벤트를 발행합니다.")
    void publishActivityStatusChangedEventWhenStatusChanged(ActivityStatus expected) {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
      String status =
          switch (expected) {
            case SUSPENDED_TEMPORARILY -> "TEMPORARY";
            case SUSPENDED_PERMANENTLY -> "PERMANENT";
            default -> expected.name();
          };

      // When
      memberProfileService.changeMemberActivityStatus(memberId, status);

      // Then
      verifyActivityStatusChangedEvent(memberId, expected.name());
    }

    @Test
    @DisplayName("심사가 반려되면 심사 거절 상태를 담은 활동 상태 변경 이벤트를 발행합니다.")
    void publishActivityStatusChangedEventWhenScreeningRejected() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeReject(memberId);

      // Then
      verifyActivityStatusChangedEvent(memberId, "REJECTED_SCREENING");
    }

    @Test
    @DisplayName("심사가 생성되면 심사 대기 상태를 담은 활동 상태 변경 이벤트를 발행합니다.")
    void publishActivityStatusChangedEventWhenScreeningWaiting() {
      // Given
      long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.changeWaiting(memberId);

      // Then
      verifyActivityStatusChangedEvent(memberId, "WAITING_SCREENING");
    }

    @Test
    @DisplayName("휴면을 해제하면 활동 전환 이벤트를 발행합니다.")
    void publishActiveEventWhenReactivated() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.DORMANT).build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.reactivate(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameActiveEvent.class));
      verifyActivityStatusChangedEvent(memberId, "ACTIVE");
    }

    @Test
    @DisplayName("심사가 승인되면 활동 전환 이벤트를 발행합니다.")
    void publishActiveEventWhenScreeningApproved() {
      // Given
      long memberId = 1L;
      Member member =
          MemberFixture.builder().id(memberId).activityStatus(ActivityStatus.INITIAL).build();
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

      // When
      memberProfileService.approveScreening(memberId);

      // Then
      assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MemberBecameActiveEvent.class));
      verifyActivityStatusChangedEvent(memberId, "ACTIVE");
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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

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
      when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> memberProfileService.markDatingExamSubmitted(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    }
  }

  private void verifyActivityStatusChangedEvent(long memberId, String activityStatus) {
    ArgumentCaptor<MemberActivityStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(MemberActivityStatusChangedEvent.class);
    Mockito.verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().memberId()).isEqualTo(memberId);
    assertThat(eventCaptor.getValue().activityStatus()).isEqualTo(activityStatus);
  }
}
