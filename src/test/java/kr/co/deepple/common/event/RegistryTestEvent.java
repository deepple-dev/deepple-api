package kr.co.deepple.common.event;

import lombok.NonNull;

public record RegistryTestEvent(String note, @NonNull EventMetadata metadata) implements Event {

  public static RegistryTestEvent of(String note) {
    return new RegistryTestEvent(note, EventMetadata.create());
  }
}
