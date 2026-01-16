package deepple.deepple.member.command.domain.member;

import deepple.deepple.common.event.Events;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.member.command.domain.member.event.MemberProfileInitializedEvent;
import deepple.deepple.member.command.domain.member.vo.KakaoId;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import deepple.deepple.member.command.domain.member.vo.Region;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class MemberTest {

    @Test
    @DisplayName("유효한 전화번호를 사용하여 멤버를 생성합니다.")
    void createMemberWithValidValueType() {
        // Given
        String phoneNumber = "01012345678";

        // When
        Member member = Member.fromPhoneNumber(phoneNumber);

        // Then
        Assertions.assertThat(member).isNotNull();
        Assertions.assertThat(member.isProfileSettingNeeded()).isTrue();
        Assertions.assertThat(member.isPermanentlySuspended()).isFalse();
        Assertions.assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        Assertions.assertThat(member.getPrimaryContactType()).isEqualTo(PrimaryContactType.NONE);
        Assertions.assertThat(member.getHeartBalance()).isEqualTo(HeartBalance.init());
        Assertions.assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.INITIAL);
        Assertions.assertThat(member.isVip()).isFalse();
        Assertions.assertThat(member.isProfilePublic()).isFalse();
    }

    @Nested
    @DisplayName("Member 의 상태 변화 메서드 테스트")
    class MemberStatusChangeTest {

        @Test
        @DisplayName("멤버의 활동 상태를 휴면 상태로 전환합니다.")
        void changeMemberActivityStatusToDormant() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            setField(member, "id", 1L);

            // When
            member.changeToDormant();

            // Then
            Assertions.assertThat(member.isActive()).isFalse();
        }

        @Test
        @DisplayName("멤버의 활동 상태를 활동중 상태로 전환합니다.")
        void changeMemberActivityStatusToActive() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            setField(member, "id", 1L);
            member.changeToDormant();

            // When
            member.changeToActive();

            // Then
            Assertions.assertThat(member.isActive()).isTrue();
        }

        @Test
        @DisplayName("멤버의 카카오 아이디를 변경합니다.")
        void changeMemberKakaoId() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
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
            Member member = Member.fromPhoneNumber("01012345678");
            String phoneNumber = "01087564321";

            // When
            member.changePrimaryContactTypeToPhoneNumber(phoneNumber);

            // Then
            Assertions.assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
        }
    }

    @Nested
    @DisplayName("gainPurchaseHeart 메서드 테스트")
    class GainPurchaseHeartMethodTest {
        @Test
        @DisplayName("멤버가 하트를 구매하면 구매 하트 잔액이 증가합니다.")
        void shouldIncreasePurchaseHeartBalanceWhenMemberPurchasesHeart() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            Long memberId = 1L;
            setField(member, "id", memberId);
            HeartAmount purchaseHeartAmount = HeartAmount.from(100L);
            HeartBalance expectedHeartBalance = HeartBalance.init().gainPurchaseHeart(purchaseHeartAmount);

            // When
            member.gainPurchaseHeart(purchaseHeartAmount);

            // Then
            Assertions.assertThat(member.getHeartBalance()).isEqualTo(expectedHeartBalance);
        }
    }

    @Nested
    @DisplayName("useHeart 메서드 테스트")
    class UseHeartMethodTest {
        @Test
        @DisplayName("멤버가 하트를 사용하면 하트 잔액이 차감됩니다.")
        void shouldDeductHeartBalanceWhenMemberUsesHeart() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            member.gainPurchaseHeart(HeartAmount.from(100L));
            HeartAmount usingheartAmount = HeartAmount.from(-10L);
            HeartAmount expectedHeartAmount = HeartAmount.from(90L);
            HeartBalance expectedHeartBalance = HeartBalance.init().gainPurchaseHeart(expectedHeartAmount);

            // When
            member.useHeart(usingheartAmount);

            // Then
            Assertions.assertThat(member.getHeartBalance()).isEqualTo(expectedHeartBalance);
        }
    }

    @Nested
    @DisplayName("gainMissionHeart 메서드 테스트")
    class GainMissionHeartMethodTest {
        @Test
        @DisplayName("멤버가 미션을 수행하면 미션 하트 잔액이 증가합니다.")
        void shouldIncreaseMissionHeartBalanceWhenMemberCompletesMission() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            HeartAmount missionHeartAmount = HeartAmount.from(100L);
            HeartBalance expectedHeartBalance = HeartBalance.init().gainMissionHeart(missionHeartAmount);

            // When
            member.gainMissionHeart(missionHeartAmount, "Daily Login");

            // Then
            Assertions.assertThat(member.getHeartBalance()).isEqualTo(expectedHeartBalance);
        }
    }

    @Nested
    @DisplayName("updateSetting 메서드 테스트")
    class UpdateSettingMethodTest {

        @Test
        @DisplayName("멤버의 설정을 업데이트합니다.")
        void shouldUpdateMemberSettings() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            Long memberId = 1L;
            setField(member, "id", memberId);
            Grade grade = Grade.GOLD;
            final boolean isVip = true;
            final boolean isPushNotificationEnabled = false;

            // When
            member.updateSetting(grade, isVip, isPushNotificationEnabled);

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
            Member member = Member.fromPhoneNumber("01012345678");

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
        @DisplayName("최초로 멤버 프로필을 초기화 시 멤버 프로필을 업데이트 하고 이벤트를 발행합니다.")
        void shouldUpdateMemberProfileAndRaiseEventWhenInitializingProfileForTheFirstTime() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");
            Long memberId = 1L;
            setField(member, "id", memberId);

            MemberProfile profile = MemberProfile.builder()
                .nickname(Nickname.from("nickname"))
                .yearOfBirth(1998)
                .gender(Gender.MALE)
                .height(175)
                .job(Job.JOB_SEARCHING)
                .hobbies(Set.of(Hobby.BOARD_GAMES, Hobby.ANIMATION))
                .mbti(Mbti.ISTJ)
                .region(Region.of(District.ANSAN_SI))
                .smokingStatus(SmokingStatus.DAILY)
                .drinkingStatus(DrinkingStatus.NONE)
                .highestEducation(HighestEducation.ASSOCIATE)
                .religion(Religion.BUDDHIST)
                .build();

            try (MockedStatic<Events> eventsMockedStatic = mockStatic(Events.class);
                MockedStatic<MemberProfileInitializedEvent> memberProfileInitializedEventMockedStatic = mockStatic(
                    MemberProfileInitializedEvent.class)
            ) {
                MemberProfileInitializedEvent memberProfileInitializedEvent = mock(MemberProfileInitializedEvent.class);
                memberProfileInitializedEventMockedStatic.when(() -> MemberProfileInitializedEvent.from(memberId))
                    .thenReturn(memberProfileInitializedEvent);

                // When
                member.updateProfile(profile);

                // Then
                memberProfileInitializedEventMockedStatic.verify(
                    () -> MemberProfileInitializedEvent.from(memberId),
                    times(1)
                );
                eventsMockedStatic.verify(
                    () -> Events.raise(memberProfileInitializedEvent),
                    times(1)
                );
                Assertions.assertThat(member.getProfile()).isEqualTo(profile);

                eventsMockedStatic.clearInvocations();

                // When
                setField(member, "activityStatus", ActivityStatus.ACTIVE);
                member.updateProfile(profile);

                // Then
                eventsMockedStatic.verifyNoInteractions();
            }
            Assertions.assertThat(member.getProfile()).isEqualTo(profile);
        }
    }

    @Nested
    @DisplayName("delete 메서드 테스트")
    class DeleteMethodTest {

        @Test
        @DisplayName("멤버를 삭제 상태로 변경하고, deletedAt 필드를 설정합니다.")
        void shouldMarkMemberAsDeleted() {
            // Given
            Member member = Member.fromPhoneNumber("01012345678");

            // When
            member.delete();

            // Then
            Assertions.assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.DELETED);
            Assertions.assertThat(member.getDeletedAt()).isNotNull();
        }
    }
}
