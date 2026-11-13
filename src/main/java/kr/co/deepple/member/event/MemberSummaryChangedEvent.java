package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;

public record MemberSummaryChangedEvent(
    long memberId, String gender, boolean isVip, @NonNull EventMetadata metadata) implements Event {

  public static MemberSummaryChangedEvent of(long memberId, String gender, boolean isVip) {
    return new MemberSummaryChangedEvent(memberId, gender, isVip, EventMetadata.create());
  }
}
