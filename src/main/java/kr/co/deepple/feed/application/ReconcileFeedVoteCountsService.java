package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.co.deepple.feed.application.dto.PollOptionVoteCount;
import kr.co.deepple.feed.application.port.in.ReconcileFeedVoteCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.domain.FeedVoteCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReconcileFeedVoteCountsService implements ReconcileFeedVoteCountsUseCase {

  private static final long RANGE_SIZE = 1000L;

  private final FeedVoteRepository feedVoteRepository;
  private final FeedVoteCountRepository feedVoteCountRepository;
  private final FeedVoteCountCacheRepository feedVoteCountCacheRepository;

  @Override
  public void reconcile() {
    long maxFeedId =
        Math.max(feedVoteRepository.findMaxFeedId(), feedVoteCountRepository.findMaxFeedId());
    for (long fromFeedId = 1L; fromFeedId <= maxFeedId; fromFeedId += RANGE_SIZE) {
      reconcileRange(fromFeedId, fromFeedId + RANGE_SIZE - 1);
    }
  }

  private void reconcileRange(long fromFeedId, long toFeedId) {
    Map<Long, Map<Long, Long>> cachedByFeed =
        feedVoteCountCacheRepository.findAll(findFeedIdsBetween(fromFeedId, toFeedId));
    Map<Long, FeedVoteCount> savedCounts = findSavedCounts(fromFeedId, toFeedId);
    Map<Long, PollOptionVoteCount> actualCounts =
        countVotes(fromFeedId, toFeedId, savedCounts.values());
    fixCachedCounts(cachedByFeed, actualCounts.values());
    fixSavedCounts(actualCounts.values(), savedCounts);
  }

  private List<Long> findFeedIdsBetween(long fromFeedId, long toFeedId) {
    return LongStream.rangeClosed(fromFeedId, toFeedId).boxed().toList();
  }

  private Map<Long, FeedVoteCount> findSavedCounts(long fromFeedId, long toFeedId) {
    return feedVoteCountRepository.findByFeedIdBetween(fromFeedId, toFeedId).stream()
        .collect(Collectors.toMap(FeedVoteCount::getOptionId, savedCount -> savedCount));
  }

  private Map<Long, PollOptionVoteCount> countVotes(
      long fromFeedId, long toFeedId, Collection<FeedVoteCount> savedCounts) {
    Map<Long, PollOptionVoteCount> voteCounts = new HashMap<>();
    savedCounts.forEach(savedCount ->
        voteCounts.put(
            savedCount.getOptionId(),
            new PollOptionVoteCount(savedCount.getFeedId(), savedCount.getOptionId(), 0L)));
    feedVoteRepository
        .countByFeedIdBetween(fromFeedId, toFeedId)
        .forEach(voteCount -> voteCounts.put(voteCount.optionId(), voteCount));
    return voteCounts;
  }

  private void fixCachedCounts(
      Map<Long, Map<Long, Long>> cachedByFeed, Collection<PollOptionVoteCount> actualCounts) {
    Map<Long, Map<Long, Long>> actualByFeed = groupByFeed(actualCounts);
    cachedByFeed.forEach((feedId, cachedCounts) -> {
      Map<Long, Long> wrongCounts =
          findWrongCounts(actualByFeed.getOrDefault(feedId, Map.of()), cachedCounts);
      if (!wrongCounts.isEmpty()) {
        feedVoteCountCacheRepository.updateIfUnchanged(feedId, cachedCounts, wrongCounts);
      }
    });
  }

  private void fixSavedCounts(
      Collection<PollOptionVoteCount> actualCounts, Map<Long, FeedVoteCount> savedCounts) {
    LocalDateTime now = LocalDateTime.now();
    actualCounts.forEach(actualCount -> {
      if (isSavedCountWrong(savedCounts.get(actualCount.optionId()), actualCount)) {
        feedVoteCountRepository.upsertVoteCount(
            actualCount.optionId(), actualCount.feedId(), actualCount.voteCount(), now);
      }
    });
  }

  private boolean isSavedCountWrong(FeedVoteCount savedCount, PollOptionVoteCount actualCount) {
    if (savedCount == null) {
      return true;
    }
    return savedCount.getVoteCount() != actualCount.voteCount();
  }

  private Map<Long, Map<Long, Long>> groupByFeed(Collection<PollOptionVoteCount> voteCounts) {
    return voteCounts.stream()
        .collect(
            Collectors.groupingBy(
                PollOptionVoteCount::feedId,
                Collectors.toMap(PollOptionVoteCount::optionId, PollOptionVoteCount::voteCount)));
  }

  private Map<Long, Long> findWrongCounts(
      Map<Long, Long> actualCounts, Map<Long, Long> cachedCounts) {
    Set<Long> optionIds = new HashSet<>(actualCounts.keySet());
    optionIds.addAll(cachedCounts.keySet());
    Map<Long, Long> wrongCounts = new HashMap<>();
    optionIds.forEach(optionId -> {
      long actualCount = actualCounts.getOrDefault(optionId, 0L);
      if (cachedCounts.getOrDefault(optionId, 0L) != actualCount) {
        wrongCounts.put(optionId, actualCount);
      }
    });
    return wrongCounts;
  }
}
