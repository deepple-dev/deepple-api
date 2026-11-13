package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberProfileRetrievedEvent(
    long retrieverId,
    long retrievedMemberId,
    Long matchRequesterId,
    Long matchResponderId,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MemberProfileRetrievedEvent of(
      long retrieverId, long retrievedMemberId, Long matchRequesterId, Long matchResponderId) {
    return new MemberProfileRetrievedEvent(
        retrieverId, retrievedMemberId, matchRequesterId, matchResponderId, EventMetadata.create());
  }
}
