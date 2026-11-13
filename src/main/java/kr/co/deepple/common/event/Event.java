package kr.co.deepple.common.event;

import java.time.Instant;
import java.util.UUID;

public interface Event {

  EventMetadata metadata();

  default UUID id() {
    return metadata().id();
  }

  default Instant occurredAt() {
    return metadata().occurredAt();
  }
}
