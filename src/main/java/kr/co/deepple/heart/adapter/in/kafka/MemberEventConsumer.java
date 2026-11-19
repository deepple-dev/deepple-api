package kr.co.deepple.heart.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberCommand;
import kr.co.deepple.heart.application.port.in.SyncHeartMemberUseCase;
import kr.co.deepple.member.event.MemberSummaryChangedEvent;
import kr.co.deepple.member.event.MemberTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("heartMemberEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MemberTopic.NAME, groupId = HeartConsumerGroup.ID)
public class MemberEventConsumer extends EventConsumer {

  private final SyncHeartMemberUseCase syncHeartMemberUseCase;

  @KafkaHandler
  public void handle(MemberSummaryChangedEvent event) {
    syncHeartMemberUseCase.syncHeartMember(
        new SyncHeartMemberCommand(
            event.memberId(), event.gender(), event.isVip(), event.occurredAt()));
  }
}
