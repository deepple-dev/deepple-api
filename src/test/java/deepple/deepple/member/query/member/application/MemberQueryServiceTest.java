package deepple.deepple.member.query.member.application;

import deepple.deepple.common.event.Events;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import deepple.deepple.member.command.application.member.exception.MemberNotFoundException;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.presentation.member.dto.MemberProfileResponse;
import deepple.deepple.member.presentation.member.dto.ProfileImageInfo;
import deepple.deepple.member.query.member.application.event.MemberProfileRetrievedEvent;
import deepple.deepple.member.query.member.application.exception.ProfileAccessDeniedException;
import deepple.deepple.member.query.member.infra.MemberQueryRepository;
import deepple.deepple.member.query.member.view.*;
import deepple.deepple.member.query.profileimage.ProfileImageQueryRepository;
import deepple.deepple.member.query.profileimage.view.ProfileImageView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {
    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private ProfileImageQueryRepository profileImageQueryRepository;

    @InjectMocks
    private MemberQueryService memberQueryService;

    @Nested
    @DisplayName("getInfoCache 메서드 테스트.")
    class getInfoCacheTest {

        @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
        @Test
        void throwsExceptionWhenResultIsEmpty() {
            // Given
            Long memberId = 1L;
            when(memberQueryRepository.findInfoByMemberId(memberId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberQueryService.getInfoCache(memberId))
                .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProfile 메서드 테스트.")
    class getProfileTest {
        @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
        @Test
        void throwsExceptionWhenResultIsEmpty() {
            // Given
            Long memberId = 1L;
            when(memberQueryRepository.findProfileByMemberId(memberId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberQueryService.getProfile(memberId))
                .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @DisplayName("getContact 메서드 테스트")
    @Nested
    class getContactTest {
        @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
        @Test
        void throwsExceptionWhenResultIsEmpty() {
            // Given
            Long memberId = 1L;
            when(memberQueryRepository.findContactsByMemberId(memberId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberQueryService.getContact(memberId))
                .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @DisplayName("getHeartBalance 메서드 테스트")
    @Nested
    class getHeartBalanceTest {
        @DisplayName("해당 결과가 존재하지 않을 경우, 예외 발생.")
        @Test
        void throwsExceptionWhenResultIsEmpty() {
            // Given
            Long memberId = 1L;
            when(memberQueryRepository.findHeartBalanceByMemberId(memberId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberQueryService.getHeartBalance(memberId))
                .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @DisplayName("getMemberProfile 메서드 테스트")
    @Nested
    class getMemberProfileTest {

        Long memberId = 1L;
        Long otherMemberId = 2L;

        static Stream<Arguments> authorizedCaseWithDescription() {
            return Stream.of(
                Arguments.of(
                    new ProfileAccessView(true, null, null, null, null, null, false, false,
                        ActivityStatus.ACTIVE.name()),
                    "이상형으로 소개받은 경우, 권한 존재."),
                Arguments.of(
                    new ProfileAccessView(false, 2L, 1L, null, null, null, false, false, ActivityStatus.ACTIVE.name()),
                    "매치 요청을 받은 경우, 권한 존재."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, 2L, 1L, ProfileExchangeStatus.WAITING.name(), false, false,
                        ActivityStatus.ACTIVE.name()),
                    "프로필 교환 요청을 받은 경우, 권한 존재."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, 2L, 1L, ProfileExchangeStatus.APPROVE.name(), false, false,
                        ActivityStatus.ACTIVE.name()),
                    "프로필 교환 요청을 수락 받은 경우, 권한 존재."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, null, null, null, true, false,
                        ActivityStatus.ACTIVE.name()),
                    "좋아요를 받은 경우, 권한 존재.")
            );
        }

        static Stream<Arguments> notAuthorizedCaseWithDescription() {
            return Stream.of(
                Arguments.of(
                    new ProfileAccessView(false, 1L, 2L, null, null, null, false, false, ActivityStatus.ACTIVE.name()),
                    "매치 신청을 한 경우, 권한이 존재하지 않는다."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, 1L, 2L, ProfileExchangeStatus.WAITING.name(), false, false,
                        ActivityStatus.ACTIVE.name()),
                    "프로필 교환 요청이 대기중인 경우, 권한이 존재하지 않는다."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, 1L, 2L, ProfileExchangeStatus.REJECTED.name(), false,
                        false, ActivityStatus.ACTIVE.name()),
                    "프로필 교환 요청이 거절된 경우, 권한이 존재하지 않는다."),
                Arguments.of(
                    new ProfileAccessView(true, null, null, null, null, null, false, true,
                        ActivityStatus.ACTIVE.name()),
                    "차단 당한 경우 권한이 존재하지 않는다."),
                Arguments.of(
                    new ProfileAccessView(true, null, null, null, null, null, false, true,
                        ActivityStatus.DORMANT.name()),
                    "상대가 Active 상태가 아닌 경우 권한이 존재하지 않는다."),
                Arguments.of(
                    new ProfileAccessView(false, null, null, null, null, null, false, false,
                        ActivityStatus.ACTIVE.name()),
                    "어떠한 케이스도 존재하지 않으면, 권한이 존재하지 않는다.")
            );
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("notAuthorizedCaseWithDescription")
        @DisplayName("프로필 접근 권한이 존재하는 경우, 예외가 발생한다.")
        void throwsExceptionWhenProfileAccessIsNotAuthorized(ProfileAccessView profileAccessView, String predict) {
            // Given
            when(memberQueryRepository.findProfileAccessViewByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(profileAccessView));

            // When & Then
            assertThatThrownBy(() -> memberQueryService.getMemberProfile(memberId, otherMemberId))
                .isInstanceOf(ProfileAccessDeniedException.class);
        }

        @ParameterizedTest(name = "{1}")
        @MethodSource("authorizedCaseWithDescription")
        @DisplayName("프로필 접근 권한이 존재하는 경우, 예외를 발생하지 않는다.")
        void notThrowsExceptionWhenProfileAccessIsAuthorized(ProfileAccessView profileAccessView, String predict) {
            // Given
            OtherMemberProfileView view = new OtherMemberProfileView(mock(BasicMemberInfo.class), mock(MatchInfo.class),
                mock(ContactView.class), mock(ProfileExchangeInfo.class), mock(IntroductionInfo.class));
            when(memberQueryRepository.findProfileAccessViewByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(profileAccessView));
            when(memberQueryRepository.findOtherProfileByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(view));

            try (MockedStatic<Events> mockEvents = mockStatic(Events.class);
                MockedStatic<MemberProfileRetrievedEvent> mockMemberProfileRetrievedEvent = mockStatic(
                    MemberProfileRetrievedEvent.class)
            ) {
                MemberProfileRetrievedEvent memberProfileRetrievedEvent = mock(MemberProfileRetrievedEvent.class);
                mockMemberProfileRetrievedEvent.when(
                        () -> MemberProfileRetrievedEvent.of(memberId, otherMemberId, profileAccessView.matchRequesterId(),
                            profileAccessView.matchResponderId()))
                    .thenReturn(memberProfileRetrievedEvent);

                // When
                memberQueryService.getMemberProfile(memberId, otherMemberId);

                // Then
                mockEvents.verify(() -> Events.raise(memberProfileRetrievedEvent));
            }
        }

        @DisplayName("상대방 프로필 조회 시 메인 이미지를 제외한 추가 이미지를 순서대로 반환한다.")
        @Test
        void returnsAdditionalProfileImagesExcludingPrimary() {
            // Given
            ProfileAccessView profileAccessView = new ProfileAccessView(true, null, null, null, null, null, false,
                false,
                ActivityStatus.ACTIVE.name());
            OtherMemberProfileView view = new OtherMemberProfileView(mock(BasicMemberInfo.class), mock(MatchInfo.class),
                mock(ContactView.class), mock(ProfileExchangeInfo.class), mock(IntroductionInfo.class));
            when(memberQueryRepository.findProfileAccessViewByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(profileAccessView));
            when(memberQueryRepository.findOtherProfileByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(view));
            when(profileImageQueryRepository.findByMemberId(otherMemberId)).thenReturn(List.of(
                new ProfileImageView(1L, "https://image/main.jpg", true, 0),
                new ProfileImageView(2L, "https://image/sub1.jpg", false, 1),
                new ProfileImageView(3L, "https://image/sub2.jpg", false, 2)
            ));

            // When
            MemberProfileResponse response;
            try (MockedStatic<Events> mockEvents = mockStatic(Events.class);
                MockedStatic<MemberProfileRetrievedEvent> mockMemberProfileRetrievedEvent = mockStatic(
                    MemberProfileRetrievedEvent.class)) {
                response = memberQueryService.getMemberProfile(memberId, otherMemberId);
            }

            // Then
            assertThat(response.memberInfo().additionalProfileImages())
                .extracting(ProfileImageInfo::url, ProfileImageInfo::order)
                .containsExactly(
                    tuple("https://image/sub1.jpg", 1),
                    tuple("https://image/sub2.jpg", 2));
        }

        @DisplayName("메인 이미지만 존재하는 경우, 추가 이미지 목록은 비어 있다.")
        @Test
        void returnsEmptyAdditionalProfileImagesWhenOnlyPrimaryExists() {
            // Given
            ProfileAccessView profileAccessView = new ProfileAccessView(true, null, null, null, null, null, false,
                false,
                ActivityStatus.ACTIVE.name());
            OtherMemberProfileView view = new OtherMemberProfileView(mock(BasicMemberInfo.class), mock(MatchInfo.class),
                mock(ContactView.class), mock(ProfileExchangeInfo.class), mock(IntroductionInfo.class));
            when(memberQueryRepository.findProfileAccessViewByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(profileAccessView));
            when(memberQueryRepository.findOtherProfileByMemberId(memberId, otherMemberId)).thenReturn(
                Optional.of(view));
            when(profileImageQueryRepository.findByMemberId(otherMemberId)).thenReturn(List.of(
                new ProfileImageView(1L, "https://image/main.jpg", true, 0)
            ));

            // When
            MemberProfileResponse response;
            try (MockedStatic<Events> mockEvents = mockStatic(Events.class);
                MockedStatic<MemberProfileRetrievedEvent> mockMemberProfileRetrievedEvent = mockStatic(
                    MemberProfileRetrievedEvent.class)) {
                response = memberQueryService.getMemberProfile(memberId, otherMemberId);
            }

            // Then
            assertThat(response.memberInfo().additionalProfileImages()).isEmpty();
        }
    }
}
