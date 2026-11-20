package kr.co.deepple.datingexam.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(DatingExamTopic.NAME + "::#{#this.memberId()}")
public record DominantPersonalityTypeChangedEvent(
    long memberId, @NonNull String personalityType, @NonNull EventMetadata metadata)
    implements Event {

  public static DominantPersonalityTypeChangedEvent of(long memberId, String personalityType) {
    return new DominantPersonalityTypeChangedEvent(
        memberId, personalityType, EventMetadata.create());
  }
}
