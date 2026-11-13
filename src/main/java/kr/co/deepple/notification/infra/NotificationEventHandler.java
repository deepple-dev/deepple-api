package kr.co.deepple.notification.infra;

import java.util.Map;
import kr.co.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import kr.co.deepple.admin.command.domain.warning.WarningIssuedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.command.domain.profileexchange.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.heart.domain.event.AdminHeartGrantedEvent;
import kr.co.deepple.like.command.domain.LikeSentEvent;
import kr.co.deepple.match.command.domain.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.command.domain.match.event.MatchRejectedEvent;
import kr.co.deepple.match.command.domain.match.event.MatchRequestedEvent;
import kr.co.deepple.notification.command.application.NotificationSendRequest;
import kr.co.deepple.notification.command.application.NotificationSendService;
import kr.co.deepple.notification.command.domain.ChannelType;
import kr.co.deepple.notification.command.domain.NotificationType;
import kr.co.deepple.notification.command.domain.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandler {

  public static final String SENDER_NAME = "senderName";

  private final NotificationSendService notificationSendService;

  @Async
  @TransactionalEventListener(
      value = MatchRequestedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMatchRequestedEvent(MatchRequestedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.requesterId(),
            event.responderId(),
            NotificationType.MATCH_REQUEST,
            Map.of(SENDER_NAME, event.requesterName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = MatchAcceptedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMatchAcceptedEvent(MatchAcceptedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.MATCH_ACCEPT,
            Map.of(SENDER_NAME, event.responderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = MatchRejectedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleMatchRejectedEvent(MatchRejectedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.MATCH_REJECT,
            Map.of(SENDER_NAME, event.responderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = ProfileExchangeRequestedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleProfileExchangeRequestedEvent(ProfileExchangeRequestedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.requesterId(),
            event.responderId(),
            NotificationType.PROFILE_EXCHANGE_REQUEST,
            Map.of(SENDER_NAME, event.senderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = ProfileExchangeAcceptedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleProfileExchangeAcceptedEvent(ProfileExchangeAcceptedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.PROFILE_EXCHANGE_ACCEPT,
            Map.of(SENDER_NAME, event.senderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = ProfileExchangeRejectedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleProfileExchangeRejectedEvent(ProfileExchangeRejectedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.responderId(),
            event.requesterId(),
            NotificationType.PROFILE_EXCHANGE_REJECT,
            Map.of(SENDER_NAME, event.senderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = WarningIssuedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleWarningIssuedEvent(WarningIssuedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.valueOf(event.reasonType()),
            Map.of(),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(value = LikeSentEvent.class, phase = TransactionPhase.AFTER_COMMIT)
  public void handleLikeSentEvent(LikeSentEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.MEMBER,
            event.senderId(),
            event.receiverId(),
            NotificationType.LIKE,
            Map.of(SENDER_NAME, event.senderName()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = ScreeningApprovedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleScreeningApprovedEvent(ScreeningApprovedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.SCREENING_APPROVED,
            Map.of(),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = ScreeningRejectedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleScreeningRejectedEvent(ScreeningRejectedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.SCREENING_REJECTED,
            Map.of("rejectionReason", event.rejectionReason()),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }

  @Async
  @TransactionalEventListener(
      value = AdminHeartGrantedEvent.class,
      phase = TransactionPhase.AFTER_COMMIT)
  public void handleAdminHeartGrantedEvent(AdminHeartGrantedEvent event) {
    NotificationSendRequest request =
        new NotificationSendRequest(
            SenderType.ADMIN,
            event.adminId(),
            event.memberId(),
            NotificationType.HEART_GRANTED,
            Map.of("amount", String.valueOf(event.amount())),
            ChannelType.PUSH);
    notificationSendService.send(request);
  }
}
