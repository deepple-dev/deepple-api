package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.List;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;

public interface HomeFeedCacheRepository {

  boolean isLoaded(String grade);

  List<HomeFeedSortKey> findSortKeys(String grade, long maxScore, int count);

  void replace(String grade, List<HomeFeedSortKey> sortKeys);

  void add(String grade, HomeFeedSortKey sortKey);

  void remove(long feedId);

  void removeAll(Collection<Long> feedIds);
}
