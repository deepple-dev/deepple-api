package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FeedCommentCountCacheRepository {

  Map<Long, Long> findAll(Collection<Long> feedIds);

  void saveAllIfAbsent(Map<Long, Long> commentCounts);

  boolean exists(long feedId);

  void saveChangedIfAbsent(long feedId, long commentCount);

  boolean incrementIfPresent(long feedId);

  List<Long> popChangedFeedIds(int count);

  void updateIfUnchanged(long feedId, long expectedCount, long commentCount);

  void delete(long feedId);
}
