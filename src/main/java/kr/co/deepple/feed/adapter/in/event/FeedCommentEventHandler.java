package kr.co.deepple.feed.adapter.in.event;

import kr.co.deepple.feed.application.port.in.DeleteCommentsOfFeedUseCase;
import kr.co.deepple.feed.domain.FeedDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCommentEventHandler {

  private final DeleteCommentsOfFeedUseCase deleteCommentsOfFeedUseCase;

  @ApplicationModuleListener
  public void handle(FeedDeletedEvent event) {
    deleteCommentsOfFeedUseCase.deleteCommentsOfFeed(event.feedId());
  }
}
