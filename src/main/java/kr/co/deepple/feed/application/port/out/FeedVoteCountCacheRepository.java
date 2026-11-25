package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FeedVoteCountCacheRepository {

  Map<Long, Map<Long, Long>> findAll(Collection<Long> feedIds);

  void saveAllIfAbsent(Map<Long, Map<Long, Long>> voteCounts);

  boolean exists(long feedId);

  void saveChangedIfAbsent(long feedId, Map<Long, Long> voteCounts);

  boolean incrementIfPresent(long feedId, long optionId);

  List<Long> popChangedFeedIds(int count);

  void updateIfUnchanged(
      long feedId, Map<Long, Long> expectedCounts, Map<Long, Long> voteCounts);

  void delete(long feedId);
}
