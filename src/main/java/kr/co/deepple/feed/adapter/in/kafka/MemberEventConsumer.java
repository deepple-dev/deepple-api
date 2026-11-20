package kr.co.deepple.feed.adapter.in.kafka;

import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorProfileCommand;
import kr.co.deepple.feed.application.port.in.SyncFeedAuthorUseCase;
import kr.co.deepple.member.event.MemberActivityStatusChangedEvent;
import kr.co.deepple.member.event.MemberGradeChangedEvent;
import kr.co.deepple.member.event.MemberPrimaryProfileImageChangedEvent;
import kr.co.deepple.member.event.MemberProfileChangedEvent;
import kr.co.deepple.member.event.MemberTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("feedMemberEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = MemberTopic.NAME, groupId = FeedConsumerGroup.ID)
public class MemberEventConsumer extends EventConsumer {

  private final SyncFeedAuthorUseCase syncFeedAuthorUseCase;

  @KafkaHandler
  public void handle(MemberProfileChangedEvent event) {
    syncFeedAuthorUseCase.syncProfile(
        new SyncFeedAuthorProfileCommand(
            event.memberId(),
            event.nickname(),
            event.gender(),
            event.yearOfBirth(),
            event.city(),
            event.mbti(),
            event.occurredAt()));
  }

  @KafkaHandler
  public void handle(MemberPrimaryProfileImageChangedEvent event) {
    syncFeedAuthorUseCase.syncProfileImage(event.memberId(), event.imageUrl(), event.occurredAt());
  }

  @KafkaHandler
  public void handle(MemberActivityStatusChangedEvent event) {
    syncFeedAuthorUseCase.syncActivityStatus(
        event.memberId(), event.activityStatus(), event.occurredAt());
  }

  @KafkaHandler
  public void handle(MemberGradeChangedEvent event) {
    syncFeedAuthorUseCase.syncGrade(event.memberId(), event.grade(), event.occurredAt());
  }
}
