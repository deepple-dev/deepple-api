package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedSnapshot;

public interface FeedSnapshotCacheRepository {

  List<FeedSnapshot> findFeeds(Collection<Long> feedIds);

  void saveFeeds(Collection<FeedSnapshot> feeds);

  void invalidateFeed(long feedId);

  List<FeedAuthorSnapshot> findAuthors(Collection<Long> authorIds);

  void saveAuthors(Collection<FeedAuthorSnapshot> authors);

  void invalidateAuthor(long authorId);
}
