package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import kr.co.deepple.feed.application.port.in.LikeFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeFeedService implements LikeFeedUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedLikeRepository feedLikeRepository;
  private final FeedLikeCounter feedLikeCounter;
  private final LikedFeedCacheRepository likedFeedCacheRepository;

  @Override
  public void likeFeed(long memberId, long feedId) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    feedAccessChecker.validateAccessible(me, feedId);
    if (saveLikeIfAbsent(memberId, feedId)) {
      increaseCachedLike(memberId, feedId);
    }
  }

  private boolean saveLikeIfAbsent(long memberId, long feedId) {
    return feedLikeRepository.insertIfAbsent(feedId, memberId, LocalDateTime.now()) == 1;
  }

  private void increaseCachedLike(long memberId, long feedId) {
    feedLikeCounter.increase(feedId);
    likedFeedCacheRepository.addIfPresent(memberId, feedId);
  }
}
