package kr.co.deepple.match.command.application.match;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchErrorCode;
import kr.co.deepple.match.command.domain.match.MatchRepository;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.match.command.domain.match.event.MatchRequestCompletedEvent;
import kr.co.deepple.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.event.MatchRejectedEvent;
import kr.co.deepple.match.event.MatchRequestedEvent;
import kr.co.deepple.match.event.MatchRespondedEvent;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.match.presentation.dto.MatchRequestDto;
import kr.co.deepple.match.presentation.dto.MatchResponseDto;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import org.assertj.core.api.Assertions;
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
class MatchServiceTest {

  @Mock private MatchRepository matchRepository;

  @Mock private DistributedLockTemplate distributedLockTemplate;

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private MemberIntroductionCommandRepository introductionCommandRepository;

  @Mock private UseHeartUseCase useHeartUseCase;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MatchService matchService;

  private void mockMember(Long memberId, String nickname) {
    Member member = MemberFixture.builder().id(memberId).nickname(nickname).build();

    when(memberCommandRepository.findById(memberId)).thenReturn(Optional.of(member));
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
      MatchRequestDto requestDto =
          new MatchRequestDto(responderId, requestMessage, contactType.name());

      doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      when(matchRepository.existsActiveMatchBetween(requesterId, requestDto.responderId()))
          .thenReturn(true);

      // When & Then
      Assertions.assertThatThrownBy(() -> matchService.request(requesterId, requestDto))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("두 유저 사이의 매치가 존재하지 않는 경우 매치 생성.")
    void createMatch() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      String requestMessage = "매칭을 요청합니다!";
      MatchContactType contactType = MatchContactType.PHONE_NUMBER;
      MatchRequestDto requestDto =
          new MatchRequestDto(responderId, requestMessage, contactType.name());

      when(introductionCommandRepository.findByMemberIdAndIntroducedMemberId(
              requesterId, responderId))
          .thenReturn(Optional.empty());

      doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      when(matchRepository.existsActiveMatchBetween(requesterId, requestDto.responderId()))
          .thenReturn(false);

      mockMember(requesterId, "testUser");

      // When
      matchService.request(requesterId, requestDto);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchRequestedEvent.class));
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchRequestCompletedEvent.class));
      verify(matchRepository)
          .save(
              argThat(match ->
                  match.getRequesterId() == requesterId
                      && match.getResponderId() == responderId
                      && match.getRequestMessage().getValue().equals(requestMessage)));
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
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_NOT_FOUND);
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

      Match match =
          MatchFixture.builder()
              .requesterId(requesterId)
              .responderId(responderId)
              .status(MatchStatus.REJECTED)
              .build();

      when(matchRepository.findByIdAndResponderId(matchId, responderId))
          .thenReturn(Optional.of(match));

      // When & Then
      Assertions.assertThatThrownBy(() -> matchService.approve(matchId, responderId, responseDto))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.INVALID_MATCH_STATUS_CHANGE);
    }

    @DisplayName("매치의 상태가 대기중이며, 응답자 아이디가 일치할 경우 하트를 쓰지 않고 수락.")
    @Test
    void approveMatch() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      Long matchId = 3L;
      String responseMessage = "매치 수락할게요";
      MatchContactType contactType = MatchContactType.PHONE_NUMBER;
      Match match =
          MatchFixture.builder().requesterId(requesterId).responderId(responderId).build();

      MatchResponseDto responseDto = new MatchResponseDto(responseMessage, contactType.name());

      when(matchRepository.findByIdAndResponderId(matchId, responderId))
          .thenReturn(Optional.of(match));

      Member requester = MemberFixture.builder().id(requesterId).build();
      when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(requester));

      mockMember(responderId, "responder");

      // When
      matchService.approve(matchId, responderId, responseDto);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchRespondedEvent.class));
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchAcceptedEvent.class));
      Assertions.assertThat(match.getStatus()).isEqualTo(MatchStatus.MATCHED);
      Assertions.assertThat(match.getResponseMessage().getValue()).isEqualTo(responseMessage);
      verifyNoInteractions(useHeartUseCase);
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
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_NOT_FOUND);
    }

    @DisplayName("매치의 상태가 대기중이 아닐 경우, 예외 발생")
    @Test
    void throwsExceptionWhenStatusIsNotWaiting() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      Long matchId = 3L;
      String responseMessage = "매치 수락할게요";

      Match match =
          MatchFixture.builder()
              .requesterId(requesterId)
              .responderId(responderId)
              .status(MatchStatus.MATCHED)
              .responseMessage(responseMessage)
              .responderContactType(MatchContactType.PHONE_NUMBER)
              .build();

      when(matchRepository.findByIdAndResponderId(matchId, responderId))
          .thenReturn(Optional.of(match));

      // When & Then
      Assertions.assertThatThrownBy(() -> matchService.reject(matchId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.INVALID_MATCH_STATUS_CHANGE);
    }

    @DisplayName("매치의 상태가 대기중이며, 응답자 아이디가 일치할 경우 거절.")
    @Test
    void rejectMatch() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      Long matchId = 3L;

      Match match =
          MatchFixture.builder().requesterId(requesterId).responderId(responderId).build();

      when(matchRepository.findByIdAndResponderId(matchId, responderId))
          .thenReturn(Optional.of(match));

      Member requester = MemberFixture.builder().id(requesterId).build();
      when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(requester));

      mockMember(responderId, "responder");

      // When
      matchService.reject(matchId, responderId);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchRespondedEvent.class));
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(MatchRejectedEvent.class));
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

      Match match =
          MatchFixture.builder().requesterId(requesterId).responderId(responderId).build();

      when(matchRepository.findByIdAndRequesterId(matchId, requesterId))
          .thenReturn(Optional.of(match));

      // When & Then
      Assertions.assertThatThrownBy(() -> matchService.rejectCheck(requesterId, matchId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.INVALID_MATCH_STATUS_CHANGE);
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
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_NOT_FOUND);
    }

    @DisplayName("매치가 존재하며, 해당 상태가 거절일 경우 상태 변경")
    @Test
    void checkMatch() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      Long matchId = 3L;

      Match match =
          MatchFixture.builder()
              .requesterId(requesterId)
              .responderId(responderId)
              .status(MatchStatus.REJECTED)
              .build();

      when(matchRepository.findByIdAndRequesterId(matchId, requesterId))
          .thenReturn(Optional.of(match));

      Member requester = MemberFixture.builder().id(requesterId).build();
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
      when(matchRepository.findByIdAndResponderId(matchId, readerId)).thenReturn(Optional.empty());

      // When & Then
      Assertions.assertThatThrownBy(() -> matchService.readReceivedMatch(matchId, readerId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", MatchErrorCode.MATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("응답자가 호출하면 읽음 시각이 기록된다")
    void readReceivedMatch() {
      // Given
      Long requesterId = 1L;
      Long responderId = 2L;
      Long matchId = 3L;
      Match match =
          MatchFixture.builder().requesterId(requesterId).responderId(responderId).build();

      when(matchRepository.findByIdAndResponderId(matchId, responderId))
          .thenReturn(Optional.of(match));

      // When
      matchService.readReceivedMatch(matchId, responderId);

      // Then
      Assertions.assertThat(match.getReadByResponderAt()).isNotNull();
    }
  }
}
