package kr.co.deepple.feed.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.domain.FeedCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCountReader {

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;
  private final FeedCommentCountCacheRepository feedCommentCountCacheRepository;
  private final FeedCountRepository feedCountRepository;

  public Map<Long, FeedCounts> read(Set<Long> feedIds) {
    Map<Long, Long> likeCounts = readLikeCounts(feedIds);
    Map<Long, Long> commentCounts = readCommentCounts(feedIds);
    return feedIds.stream()
        .collect(
            Collectors.toMap(
                Function.identity(),
                feedId -> new FeedCounts(likeCounts.get(feedId), commentCounts.get(feedId))));
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
    Map<Long, Long> loaded = loadSavedCounts(missingIds, FeedCount::getLikeCount);
    feedLikeCountCacheRepository.saveAllIfAbsent(loaded);
    return merge(cached, loaded);
  }

  private Map<Long, Long> readCommentCounts(Set<Long> feedIds) {
    Map<Long, Long> cached = feedCommentCountCacheRepository.findAll(feedIds);
    Set<Long> missingIds = findMissingIds(feedIds, cached);
    if (missingIds.isEmpty()) {
      return cached;
    }
    Map<Long, Long> loaded = loadSavedCounts(missingIds, FeedCount::getCommentCount);
    feedCommentCountCacheRepository.saveAllIfAbsent(loaded);
    return merge(cached, loaded);
  }

  private Set<Long> findMissingIds(Set<Long> feedIds, Map<Long, Long> cached) {
    return feedIds.stream()
        .filter(feedId -> !cached.containsKey(feedId))
        .collect(Collectors.toSet());
  }

  private Map<Long, Long> loadSavedCounts(Set<Long> feedIds, Function<FeedCount, Long> count) {
    List<FeedCount> savedCounts = feedCountRepository.findAllById(feedIds);
    Map<Long, Long> saved =
        savedCounts.stream().collect(Collectors.toMap(FeedCount::getFeedId, count));
    return feedIds.stream()
        .collect(Collectors.toMap(Function.identity(), feedId -> saved.getOrDefault(feedId, 0L)));
  }

  private Map<Long, Long> merge(Map<Long, Long> cached, Map<Long, Long> loaded) {
    Map<Long, Long> counts = new HashMap<>(cached);
    counts.putAll(loaded);
    return counts;
  }
}
