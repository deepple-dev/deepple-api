package kr.co.deepple.feed.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import kr.co.deepple.feed.domain.FeedVoteCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedVoteCountReader {

  private final FeedVoteCountCacheRepository feedVoteCountCacheRepository;
  private final FeedVoteCountRepository feedVoteCountRepository;

  public Map<Long, Map<Long, Long>> read(Collection<FeedSnapshot> pollFeeds) {
    Map<Long, Map<Long, Long>> cached =
        feedVoteCountCacheRepository.findAll(pollFeeds.stream().map(FeedSnapshot::feedId).toList());
    List<FeedSnapshot> missingFeeds =
        pollFeeds.stream().filter(feed -> !cached.containsKey(feed.feedId())).toList();
    if (missingFeeds.isEmpty()) {
      return cached;
    }
    Map<Long, Map<Long, Long>> loaded = loadVoteCounts(missingFeeds);
    feedVoteCountCacheRepository.saveAllIfAbsent(loaded);
    Map<Long, Map<Long, Long>> voteCounts = new HashMap<>(cached);
    voteCounts.putAll(loaded);
    return voteCounts;
  }

  public Map<Long, Long> read(FeedSnapshot pollFeed) {
    return read(List.of(pollFeed)).get(pollFeed.feedId());
  }

  private Map<Long, Map<Long, Long>> loadVoteCounts(List<FeedSnapshot> pollFeeds) {
    Map<Long, Long> saved =
        feedVoteCountRepository
            .findByFeedIdIn(pollFeeds.stream().map(FeedSnapshot::feedId).toList())
            .stream()
            .collect(Collectors.toMap(FeedVoteCount::getOptionId, FeedVoteCount::getVoteCount));
    return pollFeeds.stream()
        .collect(Collectors.toMap(FeedSnapshot::feedId, feed -> toVoteCounts(feed, saved)));
  }

  private Map<Long, Long> toVoteCounts(FeedSnapshot pollFeed, Map<Long, Long> saved) {
    return pollFeed.pollOptionIds().stream()
        .collect(
            Collectors.toMap(Function.identity(), optionId -> saved.getOrDefault(optionId, 0L)));
  }
}
