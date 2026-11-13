package kr.co.deepple.community.command.domain.selfintroduction.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record SelfIntroductionFirstWrittenEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static SelfIntroductionFirstWrittenEvent from(long memberId) {
    return new SelfIntroductionFirstWrittenEvent(memberId, EventMetadata.create());
  }
}
