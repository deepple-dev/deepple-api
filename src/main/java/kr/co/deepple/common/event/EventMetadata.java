package kr.co.deepple.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;

public record EventMetadata(@NonNull UUID id, @NonNull Instant occurredAt) {

  public static EventMetadata create() {
    return new EventMetadata(UUID.randomUUID(), Instant.now());
  }
}
