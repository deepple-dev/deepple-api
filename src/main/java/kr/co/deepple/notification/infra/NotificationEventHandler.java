package kr.co.deepple.notification.infra;

import java.util.Map;
import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import kr.co.deepple.community.event.ProfileExchangeAcceptedEvent;
import kr.co.deepple.community.event.ProfileExchangeRejectedEvent;
import kr.co.deepple.community.event.ProfileExchangeRequestedEvent;
import kr.co.deepple.heart.event.AdminHeartGrantedEvent;
import kr.co.deepple.like.event.LikeSentEvent;
import kr.co.deepple.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.event.MatchRejectedEvent;
import kr.co.deepple.match.event.MatchRequestedEvent;
import kr.co.deepple.notification.command.application.NotificationSendRequest;
import kr.co.deepple.notification.command.application.NotificationSendService;
import kr.co.deepple.notification.command.domain.ChannelType;
import kr.co.deepple.notification.command.domain.NotificationType;
import kr.co.deepple.notification.command.domain.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandler {

  public static final String SENDER_NAME = "senderName";

  private final NotificationSendService notificationSendService;

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
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
