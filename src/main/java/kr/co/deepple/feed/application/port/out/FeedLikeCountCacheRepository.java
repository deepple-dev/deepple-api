package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FeedLikeCountCacheRepository {

  Map<Long, Long> findAll(Collection<Long> feedIds);

  void saveAllIfAbsent(Map<Long, Long> likeCounts);

  void saveChangedIfAbsent(long feedId, long likeCount);

  boolean incrementIfPresent(long feedId);

  boolean decrementIfPresent(long feedId);

  List<Long> popChangedFeedIds(int count);

  void updateIfPresent(long feedId, long likeCount);

  void delete(long feedId);
}
