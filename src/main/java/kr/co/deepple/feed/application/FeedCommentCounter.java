package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCommentCounter {

  private final FeedCommentCountCacheRepository feedCommentCountCacheRepository;
  private final FeedCommentRepository feedCommentRepository;

  public void loadIfAbsent(long feedId) {
    if (!feedCommentCountCacheRepository.exists(feedId)) {
      loadCommentCount(feedId);
    }
  }

  public void increase(long feedId) {
    if (!feedCommentCountCacheRepository.incrementIfPresent(feedId)) {
      loadCommentCount(feedId);
    }
  }

  private void loadCommentCount(long feedId) {
    long commentCount = feedCommentRepository.countByFeedId(feedId);
    feedCommentCountCacheRepository.saveChangedIfAbsent(feedId, commentCount);
  }
}
