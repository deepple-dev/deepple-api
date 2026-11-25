package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.in.UnlikeFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.application.port.out.LikedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnlikeFeedService implements UnlikeFeedUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedLikeRepository feedLikeRepository;
  private final FeedLikeCounter feedLikeCounter;
  private final LikedFeedReader likedFeedReader;
  private final LikedFeedCacheRepository likedFeedCacheRepository;

  @Override
  public void unlikeFeed(long memberId, long feedId) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    feedAccessChecker.validateAccessible(me, feedId);
    feedLikeCounter.loadIfAbsent(feedId);
    likedFeedReader.loadIfAbsent(memberId);
    if (deleteLikeIfPresent(memberId, feedId)) {
      decreaseCachedLike(memberId, feedId);
    }
  }

  private boolean deleteLikeIfPresent(long memberId, long feedId) {
    return feedLikeRepository.deleteByFeedIdAndMemberId(feedId, memberId) == 1;
  }

  private void decreaseCachedLike(long memberId, long feedId) {
    feedLikeCounter.decrease(feedId);
    likedFeedCacheRepository.remove(memberId, feedId);
  }
}
