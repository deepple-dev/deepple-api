package kr.co.deepple.notification.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import kr.co.deepple.admin.command.domain.warning.WarningReasonType;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import kr.co.deepple.notification.command.application.NotificationSendRequest;
import kr.co.deepple.notification.command.application.NotificationSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

  @Mock private NotificationSendService notificationSendService;

  @InjectMocks private NotificationEventHandler notificationEventHandler;

  @ParameterizedTest
  @EnumSource(WarningReasonType.class)
  @DisplayName("경고 발급 이벤트: 모든 경고 사유가 같은 이름의 알림 타입으로 발송")
  void handleWarningIssuedEventForAllReasonTypes(WarningReasonType reasonType) {
    // given
    var event = WarningIssuedEvent.of(1L, 2L, 1L, reasonType.name(), false);

    // when
    notificationEventHandler.handleWarningIssuedEvent(event);

    // then
    var captor = ArgumentCaptor.forClass(NotificationSendRequest.class);
    verify(notificationSendService).send(captor.capture());
    assertThat(captor.getValue().notificationType().name()).isEqualTo(reasonType.name());
  }
}
