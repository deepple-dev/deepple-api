package kr.co.deepple.community.command.application.profileexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchange;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchangeRepository;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.MemberCommandRepository;
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
class ProfileExchangeServiceTest {

  Member sender = MemberFixture.builder().id(1L).nickname("닉네임").build();

  @Mock private MemberCommandRepository memberCommandRepository;

  @Mock private ProfileExchangeRepository profileExchangeRepository;

  @Mock private DistributedLockTemplate distributedLockTemplate;

  @Mock private BlockRepository blockRepository;

  @Mock private UseHeartUseCase useHeartUseCase;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProfileExchangeService profileExchangeService;

  @Nested
  @DisplayName("프로필 교환 요청")
  class Request {

    @Test
    @DisplayName("프로필 교환이 이미 존재할 경우, 예외 발생")
    void throwsExceptionWhenProfileExchangeExistsBetween() {
      // Given
      long requesterId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId))
          .thenReturn(true);

      Mockito.doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      // When & Then
      assertThatThrownBy(() -> profileExchangeService.request(requesterId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.PROFILE_EXCHANGE_ALREADY_EXISTS);
    }

    @DisplayName("프로필 교환이 존재하지 않아도, 상대가 Active 상태가 아니면, 예외를 던진다.")
    @Test
    void throwsExceptionWhenResponderNotActive() {
      // Given
      long requesterId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId))
          .thenReturn(false);
      Member responder =
          MemberFixture.builder().id(responderId).activityStatus(ActivityStatus.DORMANT).build();
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(responder));

      Mockito.doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      // When
      assertThatThrownBy(() -> profileExchangeService.request(requesterId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.PROFILE_EXCHANGE_RESPONDER_INACTIVE);
    }

    @DisplayName("프로필 교환 요청자가 응답자를 차단했다면, 예외를 던진다.")
    @Test
    void throwsExceptionWhenRequesterBlocksResponder() {
      // Given
      long requesterId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId))
          .thenReturn(false);
      Member responder = MemberFixture.builder().id(responderId).build();
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(responder));
      when(blockRepository.existsByBlockerIdAndBlockedId(requesterId, responderId))
          .thenReturn(true);

      Mockito.doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      // When
      assertThatThrownBy(() -> profileExchangeService.request(requesterId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED);
    }

    @DisplayName("프로필 교환 응답자가 요청자를 차단했다면, 예외를 던진다.")
    @Test
    void throwsExceptionWhenResponderBlocksRequester() {
      // Given
      long requesterId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId))
          .thenReturn(false);
      Member responder = MemberFixture.builder().id(responderId).build();
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(responder));
      when(blockRepository.existsByBlockerIdAndBlockedId(requesterId, responderId))
          .thenReturn(false);
      when(blockRepository.existsByBlockerIdAndBlockedId(responderId, requesterId))
          .thenReturn(true);

      Mockito.doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      // When
      assertThatThrownBy(() -> profileExchangeService.request(requesterId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.PROFILE_EXCHANGE_BLOCKED);
    }

    @DisplayName("프로필 교환이 존재하지 않으면, 프로필 교환을 생성한다.")
    @Test
    void request() {
      // Given
      long requesterId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.existsProfileExchangeBetween(requesterId, responderId))
          .thenReturn(false);
      Member responder = MemberFixture.builder().id(responderId).build();
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(responder));
      when(memberCommandRepository.findById(requesterId)).thenReturn(Optional.of(sender));
      when(blockRepository.existsByBlockerIdAndBlockedId(requesterId, responderId))
          .thenReturn(false);
      when(blockRepository.existsByBlockerIdAndBlockedId(responderId, requesterId))
          .thenReturn(false);

      Mockito.doAnswer(
              invocation -> {
                Runnable runnable = invocation.getArgument(1);
                runnable.run();
                return null;
              })
          .when(distributedLockTemplate)
          .executeInTransaction(any(), any());

      // When
      profileExchangeService.request(requesterId, responderId);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(ProfileExchangeRequestedEvent.class));
      Mockito.verify(profileExchangeRepository).save(Mockito.any(ProfileExchange.class));
    }
  }

  @Nested
  @DisplayName("프로필 교환 응답")
  class Response {

    @DisplayName("프로필 교환이 존재하지 않는 경우, 예외 발생.")
    @Test
    void throwsExceptionWhenProfileExchangeNotExists() {
      // Given
      long profileExchangeId = 1L;
      long responderId = 2L;

      when(profileExchangeRepository.findById(profileExchangeId)).thenReturn(Optional.empty());

      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(sender));

      // When & Then
      assertThatThrownBy(() -> profileExchangeService.approve(profileExchangeId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.PROFILE_EXCHANGE_NOT_FOUND);
      assertThatThrownBy(() -> profileExchangeService.reject(profileExchangeId, responderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue("errorCode", CommunityErrorCode.PROFILE_EXCHANGE_NOT_FOUND);
    }

    @DisplayName("프로필 교환의 응답자와 파라미터 응답자의 아이디가 서로 다른 경우, 예외 발생.")
    @Test
    void throwsExceptionWhenResponderIdNotEqualResponderIdInProfileExchange() {
      // Given
      long profileExchangeId = 1L;
      long requesterId = 2L;
      long responderId = 3L;
      long anotherResponderId = 4L;

      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder()
              .id(profileExchangeId)
              .requesterId(requesterId)
              .responderId(responderId)
              .build();

      when(profileExchangeRepository.findById(profileExchangeId))
          .thenReturn(Optional.of(profileExchange));
      when(memberCommandRepository.findById(anotherResponderId)).thenReturn(Optional.of(sender));

      // When & Then
      assertThatThrownBy(
              () -> profileExchangeService.approve(profileExchangeId, anotherResponderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.PROFILE_EXCHANGE_ACCESS_DENIED);
      assertThatThrownBy(() -> profileExchangeService.reject(profileExchangeId, anotherResponderId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", CommunityErrorCode.PROFILE_EXCHANGE_ACCESS_DENIED);
    }

    @DisplayName("프로필 교환 신청을 수락한다.")
    @Test
    void approve() {
      // Given
      long profileExchangeId = 1L;
      long requesterId = 2L;
      long responderId = 3L;
      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder()
              .id(profileExchangeId)
              .requesterId(requesterId)
              .responderId(responderId)
              .build();

      when(profileExchangeRepository.findById(profileExchangeId))
          .thenReturn(Optional.of(profileExchange));
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(sender));

      // When
      profileExchangeService.approve(profileExchangeId, responderId);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(ProfileExchangeAcceptedEvent.class));
      assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.APPROVE);
    }

    @DisplayName("프로필 교환 신청을 거절한다.")
    @Test
    void reject() {
      // Given
      long profileExchangeId = 1L;
      long requesterId = 2L;
      long responderId = 3L;
      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder()
              .id(profileExchangeId)
              .requesterId(requesterId)
              .responderId(responderId)
              .build();

      when(profileExchangeRepository.findById(profileExchangeId))
          .thenReturn(Optional.of(profileExchange));
      when(memberCommandRepository.findById(responderId)).thenReturn(Optional.of(sender));

      // When
      profileExchangeService.reject(profileExchangeId, responderId);

      // Then
      Mockito.verify(eventPublisher).publishEvent(Mockito.any(ProfileExchangeRejectedEvent.class));
      assertThat(profileExchange.getStatus()).isEqualTo(ProfileExchangeStatus.REJECTED);
    }
  }
}
