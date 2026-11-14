package kr.co.deepple.notification.application;

import static java.util.Objects.requireNonNullElse;

import java.util.Map;
import kr.co.deepple.notification.application.dto.NotificationSendRequest;
import kr.co.deepple.notification.application.dto.NotificationTestSendRequest;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.test-send.enabled", havingValue = "true")
public class NotificationTestSendService {

  private final NotificationSendService notificationSendService;

  public void send(long memberId, NotificationTestSendRequest request) {
    NotificationSendRequest sendRequest =
        new NotificationSendRequest(
            requireNonNullElse(request.senderType(), SenderType.MEMBER),
            requireNonNullElse(request.senderId(), memberId),
            requireNonNullElse(request.receiverId(), memberId),
            request.type(),
            requireNonNullElse(request.params(), Map.of()),
            ChannelType.PUSH);
    notificationSendService.send(sendRequest);
  }
}
