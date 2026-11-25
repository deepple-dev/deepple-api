package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.co.deepple.feed.application.port.in.ReconcileFeedLikeCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeRepository;
import kr.co.deepple.feed.domain.FeedCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReconcileFeedLikeCountsService implements ReconcileFeedLikeCountsUseCase {

  private static final long RANGE_SIZE = 1000L;

  private final FeedLikeRepository feedLikeRepository;
  private final FeedCountRepository feedCountRepository;
  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;

  @Override
  public void reconcile() {
    long maxFeedId =
        Math.max(feedLikeRepository.findMaxFeedId(), feedCountRepository.findMaxFeedId());
    for (long fromFeedId = 1L; fromFeedId <= maxFeedId; fromFeedId += RANGE_SIZE) {
      reconcileRange(fromFeedId, fromFeedId + RANGE_SIZE - 1);
    }
  }

  private void reconcileRange(long fromFeedId, long toFeedId) {
    Map<Long, Long> cachedCounts =
        feedLikeCountCacheRepository.findAll(findFeedIdsBetween(fromFeedId, toFeedId));
    Map<Long, Long> savedCounts = findSavedCounts(fromFeedId, toFeedId);
    Map<Long, Long> actualCounts = countLikes(fromFeedId, toFeedId, savedCounts.keySet());
    fixCachedCounts(cachedCounts, actualCounts);
    fixSavedCounts(actualCounts, savedCounts);
  }

  private List<Long> findFeedIdsBetween(long fromFeedId, long toFeedId) {
    return LongStream.rangeClosed(fromFeedId, toFeedId).boxed().toList();
  }

  private Map<Long, Long> findSavedCounts(long fromFeedId, long toFeedId) {
    return feedCountRepository.findByFeedIdBetween(fromFeedId, toFeedId).stream()
        .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getLikeCount));
  }

  private Map<Long, Long> countLikes(long fromFeedId, long toFeedId, Set<Long> savedFeedIds) {
    Map<Long, Long> likeCounts = new HashMap<>();
    savedFeedIds.forEach(feedId -> likeCounts.put(feedId, 0L));
    feedLikeRepository
        .countByFeedIdBetween(fromFeedId, toFeedId)
        .forEach(likeCount -> likeCounts.put(likeCount.feedId(), likeCount.likeCount()));
    return likeCounts;
  }

  private void fixCachedCounts(Map<Long, Long> cachedCounts, Map<Long, Long> actualCounts) {
    cachedCounts.forEach((feedId, cachedCount) -> {
      long actualCount = actualCounts.getOrDefault(feedId, 0L);
      if (cachedCount != actualCount) {
        feedLikeCountCacheRepository.updateIfUnchanged(feedId, cachedCount, actualCount);
      }
    });
  }

  private void fixSavedCounts(Map<Long, Long> actualCounts, Map<Long, Long> savedCounts) {
    LocalDateTime now = LocalDateTime.now();
    actualCounts.forEach((feedId, actualCount) -> {
      if (!actualCount.equals(savedCounts.get(feedId))) {
        feedCountRepository.upsertLikeCount(feedId, actualCount, now);
      }
    });
  }
}
