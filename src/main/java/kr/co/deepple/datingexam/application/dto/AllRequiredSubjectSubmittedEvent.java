package kr.co.deepple.datingexam.application.dto;

import kr.co.deepple.common.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class AllRequiredSubjectSubmittedEvent extends Event {

  private final long memberId;

  private AllRequiredSubjectSubmittedEvent(long memberId) {
    this.memberId = memberId;
  }

  public static AllRequiredSubjectSubmittedEvent from(long memberId) {
    return new AllRequiredSubjectSubmittedEvent(memberId);
  }
}
