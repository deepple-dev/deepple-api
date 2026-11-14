package kr.co.deepple.notification.application;

import java.util.List;
import java.util.Optional;
import kr.co.deepple.notification.domain.ChannelType;
import kr.co.deepple.notification.domain.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSenderResolver {

  private final List<NotificationSender> senders;

  public Optional<NotificationSender> resolve(ChannelType channelType) {
    if (channelType == null) {
      return Optional.empty();
    }

    List<NotificationSender> matchingSenders =
        senders.stream().filter(sender -> sender.channel() == channelType).toList();

    if (matchingSenders.size() > 1) {
      throw new IllegalArgumentException(
          "ChannelType: " + channelType + "에 대해 여러 sender가 발견되었습니다.");
    }

    return matchingSenders.stream().findFirst();
  }
}
