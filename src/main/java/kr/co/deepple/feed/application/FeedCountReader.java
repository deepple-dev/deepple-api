package kr.co.deepple.feed.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.domain.FeedCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCountReader {

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;
  private final FeedCountRepository feedCountRepository;

  public Map<Long, FeedCounts> read(Set<Long> feedIds) {
    Map<Long, Long> likeCounts = readLikeCounts(feedIds);
    return feedIds.stream()
        .collect(
            Collectors.toMap(
                Function.identity(), feedId -> FeedCounts.ofLikeCount(likeCounts.get(feedId))));
  }

  public FeedCounts read(long feedId) {
    return read(Set.of(feedId)).get(feedId);
  }

  private Map<Long, Long> readLikeCounts(Set<Long> feedIds) {
    Map<Long, Long> cached = feedLikeCountCacheRepository.findAll(feedIds);
    Set<Long> missingIds = findMissingIds(feedIds, cached);
    if (missingIds.isEmpty()) {
      return cached;
    }
    Map<Long, Long> loaded = loadLikeCounts(missingIds);
    feedLikeCountCacheRepository.saveAllIfAbsent(loaded);
    Map<Long, Long> likeCounts = new HashMap<>(cached);
    likeCounts.putAll(loaded);
    return likeCounts;
  }

  private Set<Long> findMissingIds(Set<Long> feedIds, Map<Long, Long> cached) {
    return feedIds.stream()
        .filter(feedId -> !cached.containsKey(feedId))
        .collect(Collectors.toSet());
  }

  private Map<Long, Long> loadLikeCounts(Set<Long> feedIds) {
    Map<Long, Long> saved =
        feedCountRepository.findAllById(feedIds).stream()
            .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getLikeCount));
    return feedIds.stream()
        .collect(Collectors.toMap(Function.identity(), feedId -> saved.getOrDefault(feedId, 0L)));
  }
}
