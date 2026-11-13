package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberBecameDormantEvent(long memberId, @NonNull EventMetadata metadata)
    implements Event {

  public static MemberBecameDormantEvent from(long memberId) {
    return new MemberBecameDormantEvent(memberId, EventMetadata.create());
  }
}
