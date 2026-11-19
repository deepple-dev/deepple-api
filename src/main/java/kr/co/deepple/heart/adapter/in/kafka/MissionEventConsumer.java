package kr.co.deepple.heart.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.heart.application.port.in.AddMissionHeartCommand;
import kr.co.deepple.heart.application.port.in.AddMissionHeartUseCase;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import kr.co.deepple.mission.event.MissionTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("heartMissionEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MissionTopic.NAME, groupId = HeartConsumerGroup.ID)
public class MissionEventConsumer extends EventConsumer {

  private final AddMissionHeartUseCase addMissionHeartUseCase;

  @KafkaHandler
  public void handle(MemberMissionCompletedEvent event) {
    addMissionHeartUseCase.addMissionHeart(
        new AddMissionHeartCommand(
            event.memberId(),
            event.rewardHeartAmount(),
            event.actionType(),
            event.memberMissionId() + ":" + event.successCount()));
  }
}
