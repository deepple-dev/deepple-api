package kr.co.deepple.admin.command.domain.suspension;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberUnsuspendedEvent extends Event {

  private final long memberId;

  public static MemberUnsuspendedEvent of(long memberId) {
    return new MemberUnsuspendedEvent(memberId);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
