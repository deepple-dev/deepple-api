package deepple.deepple.member.command.domain.member;

import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartBalance;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.event.MemberBecameActiveEvent;
import deepple.deepple.member.command.domain.member.event.MemberBecameDormantEvent;
import deepple.deepple.member.command.domain.member.event.MemberProfileInitializedEvent;
import deepple.deepple.member.command.domain.member.event.MemberSettingUpdatedEvent;
import deepple.deepple.member.command.domain.member.vo.KakaoId;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.fixture.MemberFixture;
import deepple.deepple.member.fixture.MemberProfileFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.argThat;
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
            Member member = MemberFixture.builder().id(1L).activityStatus(ActivityStatus.WAITING_SCREENING).build();

            try (MockedStatic<Events> events = mockStatic(Events.class)) {
                // When
                member.approveScreening();

                // Then
                Assertions.assertThat(member.isActive()).isTrue();
                events.verify(() -> Events.raise(any(MemberBecameActiveEvent.class)));
            }
        }

        @Test
        @DisplayName("활동 중인 회원은 휴면으로 전환할 수 있다.")
        void deactivateWhenActive() {
            // Given
            Member member = MemberFixture.builder().id(1L).activityStatus(ActivityStatus.ACTIVE).build();

            try (MockedStatic<Events> events = mockStatic(Events.class)) {
                // When
                member.deactivate();

                // Then
                Assertions.assertThat(member.isDormant()).isTrue();
                events.verify(() -> Events.raise(any(MemberBecameDormantEvent.class)));
            }
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

            try (MockedStatic<Events> events = mockStatic(Events.class)) {
                // When
                member.reactivate();

                // Then
                Assertions.assertThat(member.isActive()).isTrue();
                events.verify(() -> Events.raise(any(MemberBecameActiveEvent.class)));
            }
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
    @DisplayName("gainPurchaseHeart 메서드 테스트")
    class GainPurchaseHeartMethodTest {
        @Test
        @DisplayName("멤버가 하트를 구매하면 구매 하트 잔액이 증가합니다.")
        void shouldIncreasePurchaseHeartBalanceWhenMemberPurchasesHeart() {
            // Given
            Long memberId = 1L;
            Member member = MemberFixture.builder().id(memberId).build();
            long purchaseHeartAmount = 100L;
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
            Member member = MemberFixture.builder().purchaseHeartBalance(100L).build();
            HeartBalance expectedHeartBalance = HeartBalance.of(0L, 90L);

            // When
            member.useHeart(10L);

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
            Member member = MemberFixture.builder().build();
            long missionHeartAmount = 100L;
            HeartBalance expectedHeartBalance = HeartBalance.init().gainMissionHeart(missionHeartAmount);

            // When
            member.gainMissionHeart(missionHeartAmount, "Daily Login");

            // Then
            Assertions.assertThat(member.getHeartBalance()).isEqualTo(expectedHeartBalance);
        }
    }

    @Nested
    @DisplayName("gainAdminGrantedHeart 메서드 테스트")
    class GainAdminGrantedHeartMethodTest {
        @Test
        @DisplayName("관리자가 하트를 지급하면 미션 하트 잔액이 증가하고 AdminHeartGrantedEvent 가 발행됩니다.")
        void shouldIncreaseMissionHeartBalanceAndRaiseEventWhenAdminGrants() {
            try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
                // Given
                Long memberId = 1L;
                Member member = MemberFixture.builder().id(memberId).build();
                long grantAmount = 50L;
                Long adminId = 99L;
                String reason = "CS 보상";
                HeartBalance expectedHeartBalance = HeartBalance.init().gainMissionHeart(grantAmount);

                // When
                member.gainAdminGrantedHeart(grantAmount, adminId, reason);

                // Then
                Assertions.assertThat(member.getHeartBalance()).isEqualTo(expectedHeartBalance);
                eventsMock.verify(() -> Events.raise(argThat(event ->
                    event instanceof deepple.deepple.member.command.domain.member.event.AdminHeartGrantedEvent e
                        && e.getMemberId().equals(memberId)
                        && e.getAdminId().equals(adminId)
                        && e.getAmount().equals(50L)
                        && e.getReason().equals(reason)
                        && e.getMissionHeartBalance().equals(expectedHeartBalance.getMissionHeartBalance())
                        && e.getPurchaseHeartBalance().equals(expectedHeartBalance.getPurchaseHeartBalance())
                )));
            }
        }
    }

    @Nested
    @DisplayName("updateSetting 메서드 테스트")
    class UpdateSettingMethodTest {

        @Test
        @DisplayName("멤버의 설정을 업데이트합니다.")
        void shouldUpdateMemberSettings() {
            try (MockedStatic<Events> eventsMock = mockStatic(Events.class)) {
                // Given
                Long memberId = 1L;
                Member member = MemberFixture.builder().id(memberId).build();
                Grade grade = Grade.GOLD;
                final boolean isVip = true;
                final boolean isPushNotificationEnabled = false;

                // When
                member.updateSetting(grade, isVip, isPushNotificationEnabled);

                // Then
                Assertions.assertThat(member.getGrade()).isEqualTo(grade);
                Assertions.assertThat(member.isVip()).isEqualTo(isVip);
                eventsMock.verify(() -> Events.raise(argThat(event ->
                    event instanceof MemberSettingUpdatedEvent &&
                        ((MemberSettingUpdatedEvent) event).getMemberId() == memberId &&
                        !((MemberSettingUpdatedEvent) event).isPushNotificationEnabled()
                )));
            }
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
        @DisplayName("최초로 멤버 프로필을 초기화 시 멤버 프로필을 업데이트 하고 이벤트를 발행합니다.")
        void shouldUpdateMemberProfileAndRaiseEventWhenInitializingProfileForTheFirstTime() {
            // Given
            Long memberId = 1L;
            Member member = MemberFixture.builder()
                .id(memberId)
                .activityStatus(ActivityStatus.INITIAL)
                .noProfile()
                .build();

            MemberProfile profile = MemberProfileFixture.builder().build();

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
            Member member = MemberFixture.builder().build();

            // When
            member.delete();

            // Then
            Assertions.assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.DELETED);
            Assertions.assertThat(member.getDeletedAt()).isNotNull();
        }
    }
}
