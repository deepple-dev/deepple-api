package kr.co.deepple.heart.domain.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminHeartGrantedEvent extends Event {

  private final long memberId;
  private final long adminId;
  private final long amount;

  public static AdminHeartGrantedEvent of(long memberId, long adminId, long amount) {
    return new AdminHeartGrantedEvent(memberId, adminId, amount);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
