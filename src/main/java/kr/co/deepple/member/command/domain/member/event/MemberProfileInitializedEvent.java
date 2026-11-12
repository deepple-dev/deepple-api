package kr.co.deepple.member.command.domain.member.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileInitializedEvent extends Event {

  private final long memberId;

  public static MemberProfileInitializedEvent from(long memberId) {
    return new MemberProfileInitializedEvent(memberId);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
