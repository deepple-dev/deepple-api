package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.ChannelType;
import deepple.deepple.notification.command.domain.SenderType;
import deepple.deepple.notification.presentation.NotificationTestSendRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static deepple.deepple.notification.command.domain.NotificationType.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationTestSendServiceTest {

    @Mock
    private NotificationSendService notificationSendService;

    @InjectMocks
    private NotificationTestSendService notificationTestSendService;

    @Test
    @DisplayName("send(): 생략한 값은 본인·MEMBER·빈 파라미터로 발송")
    void sendWithDefaults() {
        // given
        long memberId = 1L;
        var request = new NotificationTestSendRequest(LIKE, null, null, null, null);

        // when
        notificationTestSendService.send(memberId, request);

        // then
        var sent = captureSentRequest();
        assertThat(sent.senderType()).isEqualTo(SenderType.MEMBER);
        assertThat(sent.senderId()).isEqualTo(memberId);
        assertThat(sent.receiverId()).isEqualTo(memberId);
        assertThat(sent.notificationType()).isEqualTo(LIKE);
        assertThat(sent.params()).isEmpty();
        assertThat(sent.channelType()).isEqualTo(ChannelType.PUSH);
    }

    @Test
    @DisplayName("send(): 지정한 값으로 발송")
    void sendWithGivenValues() {
        // given
        var params = Map.of("senderName", "테스트");
        var request = new NotificationTestSendRequest(LIKE, 2L, SenderType.ADMIN, 3L, params);

        // when
        notificationTestSendService.send(1L, request);

        // then
        var sent = captureSentRequest();
        assertThat(sent.senderType()).isEqualTo(SenderType.ADMIN);
        assertThat(sent.senderId()).isEqualTo(3L);
        assertThat(sent.receiverId()).isEqualTo(2L);
        assertThat(sent.params()).isEqualTo(params);
    }

    private NotificationSendRequest captureSentRequest() {
        var captor = ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationSendService).send(captor.capture());
        return captor.getValue();
    }
}
