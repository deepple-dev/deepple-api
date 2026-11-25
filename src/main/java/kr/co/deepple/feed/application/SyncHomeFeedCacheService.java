package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.common.lock.DistributedLockTemplate;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.in.SyncHomeFeedCacheUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.application.port.out.FeedSnapshotCacheRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedGrades;
import kr.co.deepple.feed.domain.UserFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncHomeFeedCacheService implements SyncHomeFeedCacheUseCase {

  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final HomeFeedCacheRepository homeFeedCacheRepository;
  private final FeedSnapshotCacheRepository feedSnapshotCacheRepository;
  private final DistributedLockTemplate distributedLockTemplate;

  @Override
  public void addFeed(long feedId) {
    distributedLockTemplate.executeInTransaction(
        HomeFeedListLoader.LOCK_KEY,
        () ->
            feedRepository
                .findByIdAndDeletedAtIsNull(feedId)
                .filter(Feed::isPublished)
                .ifPresent(this::addToHome));
  }

  @Override
  public void syncFeed(long feedId) {
    feedSnapshotCacheRepository.invalidateFeed(feedId);
    distributedLockTemplate.executeInTransaction(
        HomeFeedListLoader.LOCK_KEY, () -> syncHomeFeed(feedId));
  }

  private void syncHomeFeed(long feedId) {
    Optional<Feed> published =
        feedRepository.findByIdAndDeletedAtIsNull(feedId).filter(Feed::isPublished);
    Set<String> visibleGrades = published.map(Feed::getVisibleGrades).orElse(Set.of());
    published.ifPresent(this::addToHome);
    FeedGrades.ALL.stream()
        .filter(grade -> !visibleGrades.contains(grade))
        .forEach(grade -> homeFeedCacheRepository.remove(grade, feedId));
  }

  @Override
  public void removeFeed(long feedId) {
    distributedLockTemplate.execute(
        HomeFeedListLoader.LOCK_KEY, () -> homeFeedCacheRepository.remove(feedId));
    feedSnapshotCacheRepository.invalidateFeed(feedId);
  }

  @Override
  public void evictAuthor(long authorId) {
    feedSnapshotCacheRepository.invalidateAuthor(authorId);
  }

  @Override
  public void syncAuthorFeeds(long authorId) {
    feedSnapshotCacheRepository.invalidateAuthor(authorId);
    distributedLockTemplate.executeInTransaction(
        HomeFeedListLoader.LOCK_KEY, () -> syncHomeFeeds(authorId));
  }

  private void syncHomeFeeds(long authorId) {
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
