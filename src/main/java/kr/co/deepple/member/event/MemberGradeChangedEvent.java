package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberGradeChangedEvent(
    long memberId, @NonNull String grade, @NonNull EventMetadata metadata) implements Event {

  public static MemberGradeChangedEvent of(long memberId, String grade) {
    return new MemberGradeChangedEvent(memberId, grade, EventMetadata.create());
  }
}
