package kr.co.deepple.notification.command.application;

import kr.co.deepple.notification.command.domain.ChannelType;
import kr.co.deepple.notification.command.domain.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationSenderResolver {

    private final List<NotificationSender> senders;

    public Optional<NotificationSender> resolve(ChannelType channelType) {
        if (channelType == null) {
            return Optional.empty();
        }

        var matchingSenders = senders.stream()
            .filter(sender -> sender.channel() == channelType)
            .toList();

        if (matchingSenders.size() > 1) {
            throw new IllegalArgumentException("ChannelType: " + channelType + "에 대해 여러 sender가 발견되었습니다.");
        }

        return matchingSenders.stream().findFirst();
    }
}