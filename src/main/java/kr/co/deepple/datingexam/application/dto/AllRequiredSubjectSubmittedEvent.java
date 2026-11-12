package kr.co.deepple.datingexam.application.dto;

import kr.co.deepple.common.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class AllRequiredSubjectSubmittedEvent extends Event {

  private final Long memberId;

  private AllRequiredSubjectSubmittedEvent(Long memberId) {
    this.memberId = memberId;
  }

  public static AllRequiredSubjectSubmittedEvent of(Long memberId) {
    return new AllRequiredSubjectSubmittedEvent(memberId);
  }
}
