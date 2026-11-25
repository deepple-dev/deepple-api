package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikeCounter {

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;
  private final FeedLikeRepository feedLikeRepository;

  public void loadIfAbsent(long feedId) {
    if (!feedLikeCountCacheRepository.exists(feedId)) {
      loadLikeCount(feedId);
    }
  }

  public void increase(long feedId) {
    if (!feedLikeCountCacheRepository.incrementIfPresent(feedId)) {
      loadLikeCount(feedId);
    }
  }

  public void decrease(long feedId) {
    if (!feedLikeCountCacheRepository.decrementIfPresent(feedId)) {
      loadLikeCount(feedId);
    }
  }

  private void loadLikeCount(long feedId) {
    long likeCount = feedLikeRepository.countByFeedId(feedId);
    feedLikeCountCacheRepository.saveChangedIfAbsent(feedId, likeCount);
  }
}
