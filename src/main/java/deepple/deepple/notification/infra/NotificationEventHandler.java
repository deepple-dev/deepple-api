package deepple.deepple.notification.infra;

import deepple.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import deepple.deepple.admin.command.domain.warning.WarningIssuedEvent;
import deepple.deepple.community.command.domain.profileexchange.event.ProfileExchangeAcceptedEvent;
import deepple.deepple.community.command.domain.profileexchange.event.ProfileExchangeRejectedEvent;
import deepple.deepple.community.command.domain.profileexchange.event.ProfileExchangeRequestedEvent;
import deepple.deepple.like.command.domain.LikeSentEvent;
import deepple.deepple.match.command.domain.match.event.MatchAcceptedEvent;
import deepple.deepple.match.command.domain.match.event.MatchRejectedEvent;
import deepple.deepple.match.command.domain.match.event.MatchRequestedEvent;
import deepple.deepple.member.command.domain.member.event.AdminHeartGrantedEvent;
import deepple.deepple.notification.command.application.NotificationSendRequest;
import deepple.deepple.notification.command.application.NotificationSendService;
import deepple.deepple.notification.command.domain.ChannelType;
import deepple.deepple.notification.command.domain.NotificationType;
import deepple.deepple.notification.command.domain.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandler {

    public static final String SENDER_NAME = "senderName";

    private final NotificationSendService notificationSendService;

    @Async
    @TransactionalEventListener(value = MatchRequestedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchRequestedEvent(MatchRequestedEvent event) {
        NotificationSendRequest request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getRequesterId(),
            event.getResponderId(),
            NotificationType.MATCH_REQUEST,
            Map.of(SENDER_NAME, event.getRequesterName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = MatchAcceptedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchAcceptedEvent(MatchAcceptedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getResponderId(),
            event.getRequesterId(),
            NotificationType.MATCH_ACCEPT,
            Map.of(SENDER_NAME, event.getResponderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = MatchRejectedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleMatchRejectedEvent(MatchRejectedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getResponderId(),
            event.getRequesterId(),
            NotificationType.MATCH_REJECT,
            Map.of(SENDER_NAME, event.getResponderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = ProfileExchangeRequestedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileExchangeRequestedEvent(ProfileExchangeRequestedEvent event) {
        NotificationSendRequest request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getRequesterId(),
            event.getResponderId(),
            NotificationType.PROFILE_EXCHANGE_REQUEST,
            Map.of(SENDER_NAME, event.getSenderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = ProfileExchangeAcceptedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileExchangeAcceptedEvent(ProfileExchangeAcceptedEvent event) {
        NotificationSendRequest request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getResponderId(),
            event.getRequesterId(),
            NotificationType.PROFILE_EXCHANGE_ACCEPT,
            Map.of(SENDER_NAME, event.getSenderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = ProfileExchangeRejectedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileExchangeRejectedEvent(ProfileExchangeRejectedEvent event) {
        NotificationSendRequest request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getResponderId(),
            event.getRequesterId(),
            NotificationType.PROFILE_EXCHANGE_REJECT,
            Map.of(SENDER_NAME, event.getSenderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = WarningIssuedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleWarningIssuedEvent(WarningIssuedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.ADMIN,
            event.getAdminId(),
            event.getMemberId(),
            NotificationType.valueOf(event.getReasonType()),
            Map.of(),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = LikeSentEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeSentEvent(LikeSentEvent event) {
        var request = new NotificationSendRequest(
            SenderType.MEMBER,
            event.getSenderId(),
            event.getReceiverId(),
            NotificationType.LIKE,
            Map.of(SENDER_NAME, event.getSenderName()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = ScreeningApprovedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleScreeningApprovedEvent(ScreeningApprovedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.ADMIN,
            event.getAdminId(),
            event.getMemberId(),
            NotificationType.SCREENING_APPROVED,
            Map.of(),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = ScreeningRejectedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleScreeningRejectedEvent(ScreeningRejectedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.ADMIN,
            event.getAdminId(),
            event.getMemberId(),
            NotificationType.SCREENING_REJECTED,
            Map.of("rejectionReason", event.getRejectionReason()),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }

    @Async
    @TransactionalEventListener(value = AdminHeartGrantedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleAdminHeartGrantedEvent(AdminHeartGrantedEvent event) {
        var request = new NotificationSendRequest(
            SenderType.ADMIN,
            event.getAdminId(),
            event.getMemberId(),
            NotificationType.HEART_GRANTED,
            Map.of("amount", String.valueOf(event.getAmount())),
            ChannelType.PUSH
        );
        notificationSendService.send(request);
    }
}