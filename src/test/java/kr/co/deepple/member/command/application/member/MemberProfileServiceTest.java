package kr.co.deepple.member.command.application.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.MockEventsExtension;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.City;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Job;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.presentation.member.dto.MemberProfileUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockEventsExtension.class, MockitoExtension.class})
class MemberProfileServiceTest {

  @Mock private MemberCommandRepository memberCommandRepository;

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
      final long memberId = 1L;
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
      final long memberId = 1L;
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
      final long memberId = 1L;
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
      final long memberId = 1L;
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
      final Long memberId = 1L;
      final String contactType = "PHONE_NUMBER";
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
      final Long memberId = 1L;
      final String contactType = null;
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
      final Long memberId = 1L;
      final List<String> contactTypes = List.of("PHONE_NUMBER", "KAKAO");
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
}
