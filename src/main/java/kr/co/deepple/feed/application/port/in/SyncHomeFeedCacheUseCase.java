package kr.co.deepple.feed.application.port.in;

public interface SyncHomeFeedCacheUseCase {

  void addFeed(long feedId);

  void syncFeed(long feedId);

  void removeFeed(long feedId);

  void evictAuthor(long authorId);

  void syncAuthorFeeds(long authorId);
}
