package kr.co.deepple.feed.adapter.in.event;

import kr.co.deepple.feed.application.port.in.SyncHomeFeedCacheUseCase;
import kr.co.deepple.feed.domain.FeedAuthorActivityChangedEvent;
import kr.co.deepple.feed.domain.FeedAuthorChangedEvent;
import kr.co.deepple.feed.domain.FeedDeletedEvent;
import kr.co.deepple.feed.domain.FeedPublishedEvent;
import kr.co.deepple.feed.domain.FeedUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Component
@RequiredArgsConstructor
public class HomeFeedCacheEventHandler {

  private final SyncHomeFeedCacheUseCase syncHomeFeedCacheUseCase;

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(FeedPublishedEvent event) {
    syncHomeFeedCacheUseCase.addFeed(event.feedId());
  }

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(FeedUpdatedEvent event) {
    syncHomeFeedCacheUseCase.syncFeed(event.feedId());
  }

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(FeedDeletedEvent event) {
    syncHomeFeedCacheUseCase.removeFeed(event.feedId());
  }

  @ApplicationModuleListener
  public void handle(FeedAuthorChangedEvent event) {
    syncHomeFeedCacheUseCase.evictAuthor(event.authorId());
  }

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(FeedAuthorActivityChangedEvent event) {
    syncHomeFeedCacheUseCase.syncAuthorFeeds(event.authorId());
  }
}
