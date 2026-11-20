package kr.co.deepple.feed.adapter.in.kafka;

import kr.co.deepple.block.event.BlockCreatedEvent;
import kr.co.deepple.block.event.BlockTopic;
import kr.co.deepple.common.event.EventConsumer;
import kr.co.deepple.feed.application.port.in.SyncFeedBlockUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component("feedBlockEventConsumer")
@RequiredArgsConstructor
@KafkaListener(topics = BlockTopic.NAME, groupId = FeedConsumerGroup.ID)
public class BlockEventConsumer extends EventConsumer {

  private final SyncFeedBlockUseCase syncFeedBlockUseCase;

  @KafkaHandler
  public void handle(BlockCreatedEvent event) {
    syncFeedBlockUseCase.syncBlock(event.blockerId(), event.blockedId());
  }
}
