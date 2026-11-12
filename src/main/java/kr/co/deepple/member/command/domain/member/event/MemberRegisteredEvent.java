package kr.co.deepple.member.command.domain.member.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MemberRegisteredEvent extends Event {

  private final long memberId;

  public static MemberRegisteredEvent from(long memberId) {
    return new MemberRegisteredEvent(memberId);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
