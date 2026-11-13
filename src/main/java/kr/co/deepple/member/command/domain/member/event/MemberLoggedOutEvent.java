package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberLoggedOutEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberLoggedOutEvent from(long memberId) {
    return new MemberLoggedOutEvent(memberId, EventMetadata.create());
  }
}
