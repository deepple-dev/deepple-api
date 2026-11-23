package kr.co.deepple.feed.adapter.in.event;

import kr.co.deepple.feed.application.port.in.DeleteFeedCountUseCase;
import kr.co.deepple.feed.domain.FeedDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCountEventHandler {

  private final DeleteFeedCountUseCase deleteFeedCountUseCase;

  @ApplicationModuleListener
  public void handle(FeedDeletedEvent event) {
    deleteFeedCountUseCase.deleteFeedCount(event.feedId());
  }
}
