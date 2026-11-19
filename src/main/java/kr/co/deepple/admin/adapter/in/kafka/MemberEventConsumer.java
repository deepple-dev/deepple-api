package kr.co.deepple.admin.adapter.in.kafka;

import kr.co.deepple.admin.application.ScreeningService;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.member.event.MemberProfileInitializedEvent;
import kr.co.deepple.member.event.MemberTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("adminMemberEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MemberTopic.NAME, groupId = AdminConsumerGroup.ID)
public class MemberEventConsumer extends EventConsumer {

  private final ScreeningService screeningService;

  @KafkaHandler
  public void handle(MemberProfileInitializedEvent event) {
    screeningService.createIfAbsent(event.memberId());
  }
}
