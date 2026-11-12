package kr.co.deepple.community.command.domain.selfintroduction.event;

import kr.co.deepple.common.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SelfIntroductionFirstWrittenEvent extends Event {

  private final long memberId;

  private SelfIntroductionFirstWrittenEvent(long memberId) {
    this.memberId = memberId;
  }

  public static SelfIntroductionFirstWrittenEvent from(long memberId) {
    return new SelfIntroductionFirstWrittenEvent(memberId);
  }
}
