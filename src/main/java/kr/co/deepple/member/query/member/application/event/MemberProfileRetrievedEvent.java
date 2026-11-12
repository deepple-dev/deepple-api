package kr.co.deepple.member.query.member.application.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileRetrievedEvent extends Event {

  private final long retrieverId;
  private final long retrievedMemberId;
  private final Long matchRequesterId;
  private final Long matchResponderId;

  public static MemberProfileRetrievedEvent of(
      long retrieverId, long retrievedMemberId, Long matchRequesterId, Long matchResponderId) {
    return new MemberProfileRetrievedEvent(
        retrieverId, retrievedMemberId, matchRequesterId, matchResponderId);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("retrieverId", retrieverId, "retrievedMemberId", retrievedMemberId);
  }
}
