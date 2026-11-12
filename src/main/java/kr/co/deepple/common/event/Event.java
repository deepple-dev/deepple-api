package kr.co.deepple.common.event;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public abstract class Event {

  private final Instant timestamp;

  protected Event() {
    timestamp = Instant.now();
  }

  public Map<String, Object> logAttributes() {
    return Map.of();
  }
}
