package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberLoggedInEvent(long memberId, @NonNull EventMetadata metadata) implements Event {

  public static MemberLoggedInEvent from(long memberId) {
    return new MemberLoggedInEvent(memberId, EventMetadata.create());
  }
}
