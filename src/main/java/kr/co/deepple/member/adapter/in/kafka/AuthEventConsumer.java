package kr.co.deepple.member.adapter.in.kafka;

import kr.co.deepple.auth.event.AuthTopic;
import kr.co.deepple.auth.event.MemberAccountLoggedInEvent;
import kr.co.deepple.auth.event.MemberAccountTokenRefreshedEvent;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.member.application.MemberAccountSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("memberAuthEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = AuthTopic.NAME, groupId = MemberConsumerGroup.ID)
public class AuthEventConsumer extends EventConsumer {

  private final MemberAccountSyncService memberAccountSyncService;

  @KafkaHandler
  public void handle(MemberAccountLoggedInEvent event) {
    memberAccountSyncService.recordLogin(event.accountId(), event.metadata().occurredAt());
  }

  @KafkaHandler
  public void handle(MemberAccountTokenRefreshedEvent event) {
    memberAccountSyncService.recordAccess(event.accountId(), event.metadata().occurredAt());
  }
}
