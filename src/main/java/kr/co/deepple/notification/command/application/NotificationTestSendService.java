package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.ChannelType;
import kr.co.deepple.notification.command.domain.SenderType;
import kr.co.deepple.notification.presentation.NotificationTestSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNullElse;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.test-send.enabled", havingValue = "true")
public class NotificationTestSendService {

    private final NotificationSendService notificationSendService;

    public void send(long memberId, NotificationTestSendRequest request) {
        var sendRequest = new NotificationSendRequest(
            requireNonNullElse(request.senderType(), SenderType.MEMBER),
            requireNonNullElse(request.senderId(), memberId),
            requireNonNullElse(request.receiverId(), memberId),
            request.type(),
            requireNonNullElse(request.params(), Map.of()),
            ChannelType.PUSH
        );
        notificationSendService.send(sendRequest);
    }
}
