package kr.co.deepple.block.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(BlockTopic.NAME + "::#{#this.blockerId()}")
public record BlockCreatedEvent(long blockerId, long blockedId, @NonNull EventMetadata metadata)
    implements Event {

  public static BlockCreatedEvent of(long blockerId, long blockedId) {
    return new BlockCreatedEvent(blockerId, blockedId, EventMetadata.create());
  }
}
