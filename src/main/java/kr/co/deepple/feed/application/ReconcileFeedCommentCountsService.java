package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.co.deepple.feed.application.port.in.ReconcileFeedCommentCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.domain.FeedCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReconcileFeedCommentCountsService implements ReconcileFeedCommentCountsUseCase {

  private static final long RANGE_SIZE = 1000L;

  private final FeedCommentRepository feedCommentRepository;
  private final FeedCountRepository feedCountRepository;
  private final FeedCommentCountCacheRepository feedCommentCountCacheRepository;

  @Override
  public void reconcile() {
    long maxFeedId =
        Math.max(feedCommentRepository.findMaxFeedId(), feedCountRepository.findMaxFeedId());
    for (long fromFeedId = 1L; fromFeedId <= maxFeedId; fromFeedId += RANGE_SIZE) {
      reconcileRange(fromFeedId, fromFeedId + RANGE_SIZE - 1);
    }
  }

  private void reconcileRange(long fromFeedId, long toFeedId) {
    Map<Long, Long> cachedCounts =
        feedCommentCountCacheRepository.findAll(findFeedIdsBetween(fromFeedId, toFeedId));
    Map<Long, Long> savedCounts = findSavedCounts(fromFeedId, toFeedId);
    Map<Long, Long> actualCounts = countComments(fromFeedId, toFeedId, savedCounts.keySet());
    fixCachedCounts(cachedCounts, actualCounts);
    fixSavedCounts(actualCounts, savedCounts);
  }

  private List<Long> findFeedIdsBetween(long fromFeedId, long toFeedId) {
    return LongStream.rangeClosed(fromFeedId, toFeedId).boxed().toList();
  }

  private Map<Long, Long> findSavedCounts(long fromFeedId, long toFeedId) {
    return feedCountRepository.findByFeedIdBetween(fromFeedId, toFeedId).stream()
        .collect(Collectors.toMap(FeedCount::getFeedId, FeedCount::getCommentCount));
  }

  private Map<Long, Long> countComments(long fromFeedId, long toFeedId, Set<Long> savedFeedIds) {
    Map<Long, Long> commentCounts = new HashMap<>();
    savedFeedIds.forEach(feedId -> commentCounts.put(feedId, 0L));
    feedCommentRepository
        .countByFeedIdBetween(fromFeedId, toFeedId)
        .forEach(
            commentCount -> commentCounts.put(commentCount.feedId(), commentCount.commentCount()));
    return commentCounts;
  }

  private void fixCachedCounts(Map<Long, Long> cachedCounts, Map<Long, Long> actualCounts) {
    cachedCounts.forEach((feedId, cachedCount) -> {
      long actualCount = actualCounts.getOrDefault(feedId, 0L);
      if (cachedCount != actualCount) {
        feedCommentCountCacheRepository.updateIfUnchanged(feedId, cachedCount, actualCount);
      }
    });
  }

  private void fixSavedCounts(Map<Long, Long> actualCounts, Map<Long, Long> savedCounts) {
    LocalDateTime now = LocalDateTime.now();
    actualCounts.forEach((feedId, actualCount) -> {
      if (!actualCount.equals(savedCounts.get(feedId))) {
        feedCountRepository.upsertCommentCount(feedId, actualCount, now);
      }
    });
  }
}
