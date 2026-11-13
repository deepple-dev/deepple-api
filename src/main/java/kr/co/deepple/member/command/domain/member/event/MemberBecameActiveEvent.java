package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberBecameActiveEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberBecameActiveEvent from(long memberId) {
    return new MemberBecameActiveEvent(memberId, EventMetadata.create());
  }
}
