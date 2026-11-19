package kr.co.deepple.notification.application;

import static kr.co.deepple.notification.domain.ChannelType.PUSH;
import static kr.co.deepple.notification.domain.NotificationStatus.CREATED;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_DEVICE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_EXCEPTION;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_PREFERENCE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_TEMPLATE_NOT_FOUND;
import static kr.co.deepple.notification.domain.NotificationStatus.FAILED_UNSUPPORTED_CHANNEL;
import static kr.co.deepple.notification.domain.NotificationStatus.REJECTED_BY_PREFERENCE;
import static kr.co.deepple.notification.domain.NotificationStatus.SENT;
import static kr.co.deepple.notification.domain.NotificationType.LIKE;
import static kr.co.deepple.notification.domain.SenderType.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.notification.application.dto.NotificationTemplateInfo;
import kr.co.deepple.notification.application.port.in.SendNotificationCommand;
import kr.co.deepple.notification.domain.DeviceRegistration;
import kr.co.deepple.notification.domain.Notification;
import kr.co.deepple.notification.domain.NotificationErrorCode;
import kr.co.deepple.notification.domain.NotificationSender;
import kr.co.deepple.notification.domain.NotificationStatus;
import kr.co.deepple.notification.fixture.DeviceRegistrationFixture;
import kr.co.deepple.notification.fixture.NotificationPreferenceFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSendServiceTest {

  private static final String KEY = "event-id";

  @Mock NotificationDataReader reader;

  @Mock NotificationDataWriter writer;

  @Mock NotificationSenderResolver notificationSenderResolver;

  @InjectMocks NotificationSendService service;

  private final List<Notification> savedNotifications = new ArrayList<>();
  private final List<NotificationStatus> savedStatuses = new ArrayList<>();

  @BeforeEach
  void recordSaves() {
    lenient()
        .doAnswer(invocation -> {
          Notification notification = invocation.getArgument(0);
          savedNotifications.add(notification);
          savedStatuses.add(notification.getStatus());
          return null;
        })
        .when(writer)
        .save(any(Notification.class));
  }

  @Test
  @DisplayName("send(): 같은 멱등 키의 알림이 이미 있으면 아무것도 하지 않는다")
  void skipWhenAlreadyHandled() {
    // given
    when(reader.existsNotification(KEY)).thenReturn(true);

    // when
    service.send(request(20L));

    // then
    assertThat(savedNotifications).isEmpty();
    verifyNoInteractions(notificationSenderResolver);
  }

  @Test
  @DisplayName("send(): 템플릿 없으면 FAILED_TEMPLATE_NOT_FOUND 상태로 저장")
  void sendSavesFailedTemplate() {
    // given
    when(reader.findTemplate(LIKE)).thenReturn(Optional.empty());

    // when
    service.send(request(2L));

    // then
    assertThat(savedStatuses).containsExactly(FAILED_TEMPLATE_NOT_FOUND);
    assertThat(savedNotifications.getFirst().getIdempotencyKey()).isEqualTo(KEY);
  }

  @Test
  @DisplayName("send(): 수신 설정 없으면 FAILED_PREFERENCE_NOT_FOUND 상태로 저장")
  void sendSavesFailedPreference() {
    // given
    givenTemplate();
    when(reader.findPreference(99L)).thenReturn(Optional.empty());

    // when
    service.send(request(99L));

    // then
    assertThat(savedStatuses).containsExactly(FAILED_PREFERENCE_NOT_FOUND);
  }

  @Test
  @DisplayName("send(): 수신 거부 시 REJECTED_BY_PREFERENCE 상태로 저장")
  void sendSavesRejectedNotification() {
    // given
    givenTemplate();
    var pref = NotificationPreferenceFixture.builder().memberId(1L).enabledGlobally(false).build();
    when(reader.findPreference(1L)).thenReturn(Optional.of(pref));

    // when
    service.send(request(1L));

    // then
    assertThat(savedStatuses).containsExactly(REJECTED_BY_PREFERENCE);
  }

  @Test
  @DisplayName("send(): 디바이스 없으면 FAILED_DEVICE_NOT_FOUND 상태로 저장")
  void sendSavesFailedDevice() {
    // given
    givenTemplate();
    givenPreference(30L);
    when(reader.findActiveDevice(30L)).thenReturn(Optional.empty());

    // when
    service.send(request(30L));

    // then
    assertThat(savedStatuses).containsExactly(FAILED_DEVICE_NOT_FOUND);
  }

  @Test
  @DisplayName("send(): 지원하지 않는 채널 시 FAILED_UNSUPPORTED_CHANNEL 상태로 저장")
  void sendSavesUnsupportedChannel() {
    // given
    givenTemplate();
    givenPreference(20L);
    givenDevice(20L);
    when(notificationSenderResolver.resolve(PUSH)).thenReturn(Optional.empty());

    // when
    service.send(request(20L));

    // then
    assertThat(savedStatuses).containsExactly(FAILED_UNSUPPORTED_CHANNEL);
  }

  @Test
  @DisplayName("send(): 알림을 먼저 저장하고 푸시한 뒤 SENT로 저장한다")
  void saveBeforePush() throws Exception {
    // given
    givenTemplate();
    givenPreference(20L);
    givenDevice(20L);
    var sender = mock(NotificationSender.class);
    when(notificationSenderResolver.resolve(PUSH)).thenReturn(Optional.of(sender));

    // when
    service.send(request(20L));

    // then
    InOrder inOrder = inOrder(writer, sender);
    inOrder.verify(writer).save(any(Notification.class));
    inOrder.verify(sender).send(any(Notification.class), any(DeviceRegistration.class));
    inOrder.verify(writer).save(any(Notification.class));
    assertThat(savedStatuses).containsExactly(CREATED, SENT);
    assertThat(savedNotifications.getFirst().getIdempotencyKey()).isEqualTo(KEY);
  }

  @Test
  @DisplayName("send(): 푸시 중 예외 발생 시 FAILED_EXCEPTION 상태로 저장")
  void sendSavesFailedException() throws Exception {
    // given
    givenTemplate();
    givenPreference(20L);
    givenDevice(20L);
    var sender = mock(NotificationSender.class);
    when(notificationSenderResolver.resolve(PUSH)).thenReturn(Optional.of(sender));
    doThrow(
            new DeeppleException(
                NotificationErrorCode.NOTIFICATION_SEND_FAILED,
                "receiverId=20",
                new RuntimeException("FCM 전송 실패")))
        .when(sender)
        .send(any(Notification.class), any(DeviceRegistration.class));

    // when
    service.send(request(20L));

    // then
    assertThat(savedStatuses).containsExactly(CREATED, FAILED_EXCEPTION);
  }

  private SendNotificationCommand request(long receiverId) {
    return new SendNotificationCommand(KEY, SYSTEM, 10L, receiverId, LIKE, Map.of(), PUSH);
  }

  private void givenTemplate() {
    when(reader.findTemplate(LIKE))
        .thenReturn(Optional.of(new NotificationTemplateInfo(1L, LIKE, "t", "b")));
  }

  private void givenPreference(long memberId) {
    when(reader.findPreference(memberId))
        .thenReturn(
            Optional.of(NotificationPreferenceFixture.builder().memberId(memberId).build()));
  }

  private void givenDevice(long memberId) {
    when(reader.findActiveDevice(memberId))
        .thenReturn(Optional.of(DeviceRegistrationFixture.builder().memberId(memberId).build()));
  }
}
