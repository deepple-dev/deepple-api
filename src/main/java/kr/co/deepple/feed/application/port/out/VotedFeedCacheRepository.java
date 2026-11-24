package kr.co.deepple.feed.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface VotedFeedCacheRepository {

  Optional<Map<Long, Long>> findVotedOptionIds(long memberId, Set<Long> feedIds);

  void save(long memberId, Map<Long, Long> votedOptionIds);

  void addIfPresent(long memberId, long feedId, long optionId);
}
