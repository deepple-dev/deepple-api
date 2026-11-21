package kr.co.deepple.feed.application;

import java.util.List;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.in.SyncHomeFeedCacheUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.FeedSnapshotCacheRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncHomeFeedCacheService implements SyncHomeFeedCacheUseCase {

  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final HomeFeedCacheRepository homeFeedCacheRepository;
  private final FeedSnapshotCacheRepository feedSnapshotCacheRepository;

  @Override
  @Transactional(readOnly = true)
  public void addFeed(long feedId) {
    feedRepository
        .findByIdAndDeletedAtIsNull(feedId)
        .filter(Feed::isPublished)
        .ifPresent(this::addToHome);
  }

  @Override
  public void removeFeed(long feedId) {
    homeFeedCacheRepository.remove(feedId);
    feedSnapshotCacheRepository.deleteFeed(feedId);
  }

  @Override
  public void evictAuthor(long authorId) {
    feedSnapshotCacheRepository.deleteAuthor(authorId);
  }

  @Override
  @Transactional(readOnly = true)
  public void syncAuthorFeeds(long authorId) {
    feedSnapshotCacheRepository.deleteAuthor(authorId);
    FeedAuthor author = feedAuthorReader.getAuthor(authorId);
    List<UserFeed> feeds = feedRepository.findPublishedUserFeedsByAuthorId(authorId);
    if (author.isActive()) {
      feeds.forEach(this::addToHome);
      return;
    }
    homeFeedCacheRepository.removeAll(feeds.stream().map(Feed::getId).toList());
  }

  private void addToHome(Feed feed) {
    HomeFeedSortKey sortKey = HomeFeedSortKey.of(feed);
    feed.getVisibleGrades().forEach(grade -> homeFeedCacheRepository.add(grade, sortKey));
  }
}
