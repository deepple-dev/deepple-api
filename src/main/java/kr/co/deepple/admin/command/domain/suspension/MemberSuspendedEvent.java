package kr.co.deepple.admin.command.domain.suspension;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSuspendedEvent extends Event {

  private final long memberId;
  private final String status;

  public static MemberSuspendedEvent of(long memberId, @NonNull String status) {
    return new MemberSuspendedEvent(memberId, status);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
