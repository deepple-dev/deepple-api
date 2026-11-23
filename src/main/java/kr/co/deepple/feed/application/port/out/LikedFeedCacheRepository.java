package kr.co.deepple.feed.application.port.out;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface LikedFeedCacheRepository {

  Optional<Set<Long>> findLikedFeedIds(long memberId, Set<Long> feedIds);

  void save(long memberId, Collection<Long> likedFeedIds);

  void addIfPresent(long memberId, long feedId);

  void remove(long memberId, long feedId);
}
