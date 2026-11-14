package kr.co.deepple.member.command.domain.member;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.command.domain.MemberErrorCode;
import kr.co.deepple.member.command.domain.member.vo.KakaoId;
import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.MemberProfileFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MemberTest {

  @Test
  @DisplayName("유효한 전화번호를 사용하여 멤버를 생성합니다.")
  void createMemberWithValidValueType() {
    // Given
    String phoneNumber = "01012345678";

    // When
    Member member = Member.register(1L, phoneNumber);

    // Then
    Assertions.assertThat(member.getId()).isEqualTo(1L);
    Assertions.assertThat(member.isProfileSettingNeeded()).isTrue();
    Assertions.assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
    Assertions.assertThat(member.getPrimaryContactType()).isEqualTo(PrimaryContactType.NONE);
    Assertions.assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.INITIAL);
    Assertions.assertThat(member.isVip()).isFalse();
    Assertions.assertThat(member.isProfilePublic()).isFalse();
  }

  @Nested
  @DisplayName("Member 의 상태 변화 메서드 테스트")
  class MemberStatusChangeTest {

    @Test
    @DisplayName("관리자는 회원 상태와 관계없이 휴면으로 바꿀 수 있다.")
    void changeActivityStatusToDormant() {
      // Given
      Member member = MemberFixture.builder().id(1L).activityStatus(ActivityStatus.INITIAL).build();

      // When
      member.changeActivityStatus(ActivityStatus.DORMANT);

      // Then
      Assertions.assertThat(member.isDormant()).isTrue();
    }

    @Test
    @DisplayName("심사가 승인되면 활동 중이 된다.")
    void approveScreening() {
      // Given
      Member member =
          MemberFixture.builder().id(1L).activityStatus(ActivityStatus.WAITING_SCREENING).build();

      // When
      member.approveScreening();

      // Then
      Assertions.assertThat(member.isActive()).isTrue();
    }

    @Test
    @DisplayName("활동 중인 회원은 휴면으로 전환할 수 있다.")
    void deactivateWhenActive() {
      // Given
      Member member = MemberFixture.builder().id(1L).activityStatus(ActivityStatus.ACTIVE).build();

      // When
      member.deactivate();

      // Then
      Assertions.assertThat(member.isDormant()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ActivityStatus.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("활동 중이 아닌 회원은 휴면으로 전환할 수 없다.")
    void rejectDeactivateWhenNotActive(ActivityStatus status) {
      // Given
      Member member = MemberFixture.builder().activityStatus(status).build();

      // When & Then
      Assertions.assertThatThrownBy(member::deactivate)
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE);
      Assertions.assertThat(member.getActivityStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("휴면 회원은 휴면을 해제해 활동 중이 된다.")
    void reactivateWhenDormant() {
      // Given
      Member member = MemberFixture.builder().id(1L).activityStatus(ActivityStatus.DORMANT).build();

      // When
      member.reactivate();

      // Then
      Assertions.assertThat(member.isActive()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ActivityStatus.class, names = "DORMANT", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("휴면이 아닌 회원은 휴면을 해제할 수 없다.")
    void rejectReactivateWhenNotDormant(ActivityStatus status) {
      // Given
      Member member = MemberFixture.builder().activityStatus(status).build();

      // When & Then
      Assertions.assertThatThrownBy(member::reactivate)
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.INVALID_ACTIVITY_STATUS_CHANGE);
      Assertions.assertThat(member.getActivityStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("멤버의 카카오 아이디를 변경합니다.")
    void changeMemberKakaoId() {
      // Given
      Member member = MemberFixture.builder().build();
      String kakaoId = "kongtae";

      // When
      member.changePrimaryContactTypeToKakao(KakaoId.from(kakaoId));

      // Then
      Assertions.assertThat(member.getKakaoId()).isEqualTo(kakaoId);
    }

    @Test
    @DisplayName("멤버의 휴대폰 번호를 변경합니다.")
    void changeMemberPhoneNumber() {
      // Given
      Member member = MemberFixture.builder().build();
      String phoneNumber = "01087564321";

      // When
      member.changePrimaryContactTypeToPhoneNumber(phoneNumber);

      // Then
      Assertions.assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
    }
  }

  @Nested
  @DisplayName("updateSetting 메서드 테스트")
  class UpdateSettingMethodTest {

    @Test
    @DisplayName("멤버의 설정을 업데이트합니다.")
    void shouldUpdateMemberSettings() {
      // Given
      Long memberId = 1L;
      Member member = MemberFixture.builder().id(memberId).build();
      Grade grade = Grade.GOLD;
      boolean isVip = true;

      // When
      member.updateSetting(grade, isVip);

      // Then
      Assertions.assertThat(member.getGrade()).isEqualTo(grade);
      Assertions.assertThat(member.isVip()).isEqualTo(isVip);
    }
  }

  @Nested
  @DisplayName("publishProfile 메서드 테스트")
  class PublishProfileMethodTest {

    @Test
    @DisplayName("멤버의 프로필을 공개합니다.")
    void shouldPublishMemberProfile() {
      // Given
      Member member = MemberFixture.builder().build();

      // When
      member.publishProfile();

      // Then
      Assertions.assertThat(member.isProfilePublic()).isTrue();
    }
  }

  @Nested
  @DisplayName("updateProfile 메서드 테스트")
  class UpdateProfileMethodTest {

    @Test
    @DisplayName("심사 전 상태에서 프로필을 채우면 프로필 초기화 상태가 되고, 활동 중이면 아닙니다.")
    void shouldBeProfileInitializedOnlyBeforeScreening() {
      // Given
      Long memberId = 1L;
      Member member =
          MemberFixture.builder()
              .id(memberId)
              .activityStatus(ActivityStatus.INITIAL)
              .noProfile()
              .build();

      MemberProfile profile = MemberProfileFixture.builder().build();

      // When
      member.updateProfile(profile);

      // Then
      Assertions.assertThat(member.isProfileInitialized()).isTrue();

      // When
      setField(member, "activityStatus", ActivityStatus.ACTIVE);
      member.updateProfile(profile);

      // Then
      Assertions.assertThat(member.isProfileInitialized()).isFalse();
      Assertions.assertThat(member.getProfile()).isEqualTo(profile);
    }
  }

  @Nested
  @DisplayName("markDatingExamSubmitted 메서드 테스트")
  class MarkDatingExamSubmittedTest {

    @Test
    @DisplayName("이미 제출 상태여도 예외 없이 제출 상태를 유지한다.")
    void keepSubmittedWhenAlreadySubmitted() {
      // Given
      Member member = MemberFixture.builder().build();
      member.markDatingExamSubmitted();

      // When
      member.markDatingExamSubmitted();

      // Then
      Assertions.assertThat(member.isDatingExamSubmitted()).isTrue();
    }
  }

  @Nested
  @DisplayName("delete 메서드 테스트")
  class DeleteMethodTest {

    @Test
    @DisplayName("멤버를 삭제 상태로 변경하고, deletedAt 필드를 설정합니다.")
    void shouldMarkMemberAsDeleted() {
      // Given
      Member member = MemberFixture.builder().build();

      // When
      member.delete();

      // Then
      Assertions.assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.DELETED);
      Assertions.assertThat(member.getDeletedAt()).isNotNull();
    }
  }
}
