package deepple.deepple.match.command.application.match;

import deepple.deepple.common.event.Events;
import deepple.deepple.common.repository.LockRepository;
import deepple.deepple.match.command.application.match.exception.ExistsMatchException;
import deepple.deepple.match.command.application.match.exception.InvalidMatchUpdateException;
import deepple.deepple.match.command.application.match.exception.MatchNotFoundException;
import deepple.deepple.match.command.domain.match.*;
import deepple.deepple.match.command.domain.match.vo.Message;
import deepple.deepple.match.presentation.dto.MatchRequestDto;
import deepple.deepple.match.presentation.dto.MatchResponseDto;
import deepple.deepple.member.command.domain.introduction.MemberIntroductionCommandRepository;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    private static MockedStatic<Events> mockedEvents;
    @Mock
    private MatchRepository matchRepository;

    @Mock
    private LockRepository lockRepository;

    @Mock
    private MemberCommandRepository memberCommandRepository;

    @Mock
    private MemberIntroductionCommandRepository introductionCommandRepository;

    @InjectMocks
    private MatchService matchService;

    @BeforeEach
    void setUp() {
        mockedEvents = mockStatic(Events.class);
        mockedEvents.when(() -> Events.raise(any()))
            .thenAnswer(invocation -> null);
    }

    @AfterEach
    void tearDown() {
        mockedEvents.close();
    }

    private void mockMember(Long memberId, String nickname) {
        Member member = mock(Member.class);
        MemberProfile profile = mock(MemberProfile.class);
        Nickname nicknameObj = mock(Nickname.class);

        when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.getProfile()).thenReturn(profile);
        when(profile.getNickname()).thenReturn(nicknameObj);
        when(nicknameObj.getValue()).thenReturn(nickname);
    }

    @Nested
    @DisplayName("매치 요청 테스트")
    class Request {
        @Test
        @DisplayName("이미 두 유저간의 진행중인 매치가 존재하는 경우, 예외 발생")
        void throwsExceptionWhenExistsMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            String requestMessage = "매칭을 요청합니다!";
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            MatchRequestDto requestDto = new MatchRequestDto(responderId, requestMessage, contactType.name());

            when(introductionCommandRepository.findByMemberIdAndIntroducedMemberId(requesterId, responderId))
                .thenReturn(Optional.empty());

            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
            }).when(lockRepository).withNamedLock(any(), any());

            when(matchRepository.existsActiveMatchBetween(requesterId, requestDto.responderId()))
                .thenReturn(true);

            mockMember(requesterId, "testUser");

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.request(requesterId, requestDto))
                .isInstanceOf(ExistsMatchException.class);
        }


        @Test
        @DisplayName("두 유저 사이의 매치가 존재하지 않는 경우 매치 생성.")
        void createMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            String requestMessage = "매칭을 요청합니다!";
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            MatchRequestDto requestDto = new MatchRequestDto(responderId, requestMessage, contactType.name());

            when(introductionCommandRepository.findByMemberIdAndIntroducedMemberId(requesterId, responderId))
                .thenReturn(Optional.empty());

            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
            }).when(lockRepository).withNamedLock(any(), any());

            when(matchRepository.existsActiveMatchBetween(requesterId, requestDto.responderId()))
                .thenReturn(false);

            mockMember(requesterId, "testUser");

            // When
            matchService.request(requesterId, requestDto);


            // Then
            verify(matchRepository).save(
                argThat(match -> match.getRequesterId().equals(requesterId) &&
                    match.getResponderId().equals(responderId) &&
                    match.getRequestMessage().getValue().equals(requestMessage))
            );
        }
    }

    @Nested
    @DisplayName("매치 수락 테스트")
    class Approve {

        @DisplayName("응답자의 아이디가 일치하지 않은 경우, 예외 발생")
        @Test
        void throwsExceptionWhenResponderIdIsNotEqual() {
            // Given
            Long responderId = 1L;
            Long matchId = 1L;
            String responseMessage = "매치 수락할게요";
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            MatchResponseDto responseDto = new MatchResponseDto(responseMessage, contactType.name());

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.approve(matchId, responderId, responseDto))
                .isInstanceOf(MatchNotFoundException.class);
        }

        @DisplayName("매치의 상태가 대기중이 아닐 경우, 예외 발생")
        @Test
        void throwsExceptionWhenStatusIsNotWaiting() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String responseMessage = "매치 수락할게요";
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            MatchResponseDto responseDto = new MatchResponseDto(responseMessage, contactType.name());
            MatchType type = MatchType.MATCH;

            Match match = Match.request(requesterId, responderId, Message.from(responseMessage), "name", type,
                contactType);
            match.reject("name");

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.of(match));

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.approve(matchId, responderId, responseDto))
                .isInstanceOf(InvalidMatchUpdateException.class);
        }

        @DisplayName("매치의 상태가 대기중이며, 응답자 아이디가 일치할 경우 수락.")
        @Test
        void approveMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String responseMessage = "매치 수락할게요";
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(requesterId, responderId, Message.from(responseMessage), "name", type,
                contactType);

            MatchResponseDto responseDto = new MatchResponseDto(responseMessage, contactType.name());

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.of(match));

            Member requester = mock(Member.class);
            when(requester.isActive()).thenReturn(true);
            when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(requester));

            mockMember(responderId, "responderName");

            // When
            matchService.approve(matchId, responderId, responseDto);


            // Then
            Assertions.assertThat(match.getStatus()).isEqualTo(MatchStatus.MATCHED);
            Assertions.assertThat(match.getResponseMessage().getValue()).isEqualTo(responseMessage);
        }
    }

    @Nested
    @DisplayName("매치 거절 테스트")
    class Reject {

        @DisplayName("응답자의 아이디가 일치하지 않은 경우, 예외 발생")
        @Test
        void throwsExceptionWhenResponderIdIsNotEqual() {
            // Given
            Long responderId = 1L;
            Long matchId = 1L;

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.reject(matchId, responderId))
                .isInstanceOf(MatchNotFoundException.class);
        }

        @DisplayName("매치의 상태가 대기중이 아닐 경우, 예외 발생")
        @Test
        void throwsExceptionWhenStatusIsNotWaiting() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String responseMessage = "매치 수락할게요";

            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;

            Match match = Match.request(requesterId, responderId, Message.from(responseMessage), "name", type,
                contactType);
            match.approve(Message.from(responseMessage), "name", contactType);

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.of(match));

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.reject(matchId, responderId))
                .isInstanceOf(InvalidMatchUpdateException.class);
        }

        @DisplayName("매치의 상태가 대기중이며, 응답자 아이디가 일치할 경우 거절.")
        @Test
        void rejectMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String requestMessage = "매치 신청할게요";
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;

            Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "name", type,
                contactType);

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.of(match));

            Member requester = mock(Member.class);
            when(requester.isActive()).thenReturn(true);
            when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(requester));

            mockMember(responderId, "responderName");

            // When
            matchService.reject(matchId, responderId);


            // Then
            Assertions.assertThat(match.getStatus()).isEqualTo(MatchStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("매치 거절 확인 테스트")
    class RejectCheck {

        @DisplayName("매치 상태가 거절이 아닌 경우, 예외 발생")
        @Test
        void throwsExceptionWhenMatchStatusIsNotRejected() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String requestMessage = "매치 신청할게요";
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;

            Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "name", type,
                contactType);

            when(matchRepository.findByIdAndRequesterId(matchId, requesterId))
                .thenReturn(Optional.of(match));

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.rejectCheck(requesterId, matchId))
                .isInstanceOf(InvalidMatchUpdateException.class);
        }

        @DisplayName("매치가 존재하지 않는 경우, 예외 발생")
        @Test
        void throwsExceptionWhenRequesterIdIsNotEqualMemberId() {
            // Given
            Long requesterId = 2L;
            Long matchId = 3L;

            when(matchRepository.findByIdAndRequesterId(matchId, requesterId))
                .thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.rejectCheck(requesterId, matchId))
                .isInstanceOf(MatchNotFoundException.class);
        }

        @DisplayName("매치가 존재하며, 해당 상태가 거절일 경우 상태 변경")
        @Test
        void checkMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            String requestMessage = "매치 신청할게요";
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;

            Match match = Match.request(requesterId, responderId, Message.from(requestMessage), "name", type,
                contactType);
            match.reject("name");

            when(matchRepository.findByIdAndRequesterId(matchId, requesterId))
                .thenReturn(Optional.of(match));

            Member requester = mock(Member.class);
            when(requester.isActive()).thenReturn(true);
            when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(requester));

            // When
            matchService.rejectCheck(requesterId, matchId);

            // Then
            Assertions.assertThat(match.getStatus()).isEqualTo(MatchStatus.REJECT_CHECKED);
        }
    }

    @Nested
    @DisplayName("받은 매치 읽음 처리 테스트")
    class ReadReceivedMatch {

        @Test
        @DisplayName("응답자가 아니어서 매치를 찾지 못하면 예외 발생")
        void throwsExceptionWhenNotResponder() {
            // Given
            Long matchId = 1L;
            Long readerId = 99L;
            when(matchRepository.findByIdAndResponderId(matchId, readerId))
                .thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.readReceivedMatch(matchId, readerId))
                .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        @DisplayName("응답자가 호출하면 읽음 시각이 기록된다")
        void readReceivedMatch() {
            // Given
            Long requesterId = 1L;
            Long responderId = 2L;
            Long matchId = 3L;
            Match match = Match.request(requesterId, responderId, Message.from("매칭을 요청합니다."),
                "name", MatchType.MATCH, MatchContactType.PHONE_NUMBER);

            when(matchRepository.findByIdAndResponderId(matchId, responderId))
                .thenReturn(Optional.of(match));

            // When
            matchService.readReceivedMatch(matchId, responderId);

            // Then
            Assertions.assertThat(match.getReadByResponderAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("매치 읽음 처리 테스트")
    class Read {
        @Test
        @DisplayName("읽는 사람의 아이디가 매치 참여자 아이디와 일치하지 않는 경우, 예외 발생")
        void throwsExceptionWhenReaderIdIsNotEqual() {
            // Given
            Long readerId = 1L;
            Long matchRequesterId = 2L;
            Long matchResponderId = 3L;

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.read(readerId, matchRequesterId, matchResponderId))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("매치가 존재하지 않는 경우, 예외 발생")
        void throwsExceptionWhenMatchNotFound() {
            // Given
            Long readerId = 2L;
            Long matchRequesterId = 2L;
            Long matchResponderId = 3L;

            when(matchRepository.findByRequesterIdAndResponderId(matchRequesterId, matchResponderId))
                .thenReturn(Optional.empty());

            // When & Then
            Assertions.assertThatThrownBy(() -> matchService.read(readerId, matchRequesterId, matchResponderId))
                .isInstanceOf(MatchNotFoundException.class);
        }

        @Test
        @DisplayName("매치 읽음 처리 성공")
        void readMatch() {
            // Given
            Long readerId = 2L;
            Long matchRequesterId = 2L;
            Long matchResponderId = 3L;

            Match match = mock(Match.class);

            when(matchRepository.findByRequesterIdAndResponderId(matchRequesterId, matchResponderId))
                .thenReturn(Optional.of(match));

            // When
            matchService.read(readerId, matchRequesterId, matchResponderId);

            // Then
            verify(match, times(1)).read(readerId);
        }
    }
}
