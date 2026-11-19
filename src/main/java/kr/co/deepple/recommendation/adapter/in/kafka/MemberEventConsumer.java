package kr.co.deepple.recommendation.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.member.event.MemberTopic;
import kr.co.deepple.recommendation.application.MemberIdealService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("recommendationMemberEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MemberTopic.NAME, groupId = RecommendationConsumerGroup.ID)
public class MemberEventConsumer extends EventConsumer {

  private final MemberIdealService memberIdealService;

  @KafkaHandler
  public void handle(MemberRegisteredEvent event) {
    memberIdealService.initIfAbsent(event.memberId());
  }
}
