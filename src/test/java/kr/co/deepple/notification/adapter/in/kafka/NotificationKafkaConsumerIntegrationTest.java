package kr.co.deepple.notification.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.match.event.MatchAcceptedEvent;
import kr.co.deepple.match.event.MatchRejectedEvent;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.notification.application.port.out.NotificationPreferenceRepository;
import kr.co.deepple.notification.application.port.out.NotificationRepository;
import kr.co.deepple.notification.domain.Notification;
import kr.co.deepple.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("notification Kafka 소비자 통합 테스트")
class NotificationKafkaConsumerIntegrationTest extends IntegrationTestSupport {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final Duration SHORTER_THAN_RETRIES = Duration.ofSeconds(10);

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Autowired private TransactionTemplate transactionTemplate;

  @Autowired private NotificationRepository notificationRepository;

  @Autowired private NotificationPreferenceRepository notificationPreferenceRepository;

  @Test
  @DisplayName("한 토픽에 섞인 이벤트를 __TypeId__ 헤더의 종류대로 처리한다")
  void handleByTypeHeader() {
    // given
    MatchRejectedEvent event = MatchRejectedEvent.of(1L, 10L, 20L, "응답자");

    // when
    publish(event);

    // then
    Notification notification = awaitNotification(event.id().toString());
    assertThat(notification.getType()).isEqualTo(NotificationType.MATCH_REJECT);
    assertThat(notification.getReceiverId()).isEqualTo(10L);
  }

  @Test
  @DisplayName("같은 이벤트를 두 번 받아도 알림은 하나만 만든다")
  void ignoreDuplicateEvent() {
    // given
    MatchRejectedEvent event = MatchRejectedEvent.of(2L, 10L, 20L, "응답자");
    MatchAcceptedEvent next = MatchAcceptedEvent.of(2L, 10L, 20L, "응답자");

    // when
    publish(event);
    publish(event);
    publish(next);

    // then
    awaitNotification(next.id().toString());
    assertThat(notificationRepository.findAll())
        .filteredOn(n -> n.getIdempotencyKey().equals(event.id().toString()))
        .hasSize(1);
  }

  @Test
  @DisplayName("안 쓰는 이벤트는 재시도 없이 버리고 다음 메시지를 처리한다")
  void ignoreUnhandledEventWithoutRetry() {
    // given
    long memberId = 77L;

    // when
    publish(MemberSummaryChangedEvent.of(memberId, "MALE", false));
    publish(MemberRegisteredEvent.from(memberId));

    // then
    await()
        .atMost(SHORTER_THAN_RETRIES)
        .until(() -> notificationPreferenceRepository.existsByMemberId(memberId));
  }

  private void publish(Object event) {
    transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
  }

  private Notification awaitNotification(String idempotencyKey) {
    return await()
        .atMost(TIMEOUT)
        .until(
            () ->
                notificationRepository.findAll().stream()
                    .filter(n -> n.getIdempotencyKey().equals(idempotencyKey))
                    .findFirst()
                    .orElse(null),
            n -> n != null);
  }
}
