package kr.co.deepple.feed.application;

import java.util.HashMap;
import java.util.Map;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedVoteCounter {

  private final FeedVoteCountCacheRepository feedVoteCountCacheRepository;
  private final FeedVoteRepository feedVoteRepository;

  public void loadIfAbsent(FeedSnapshot pollFeed) {
    if (!feedVoteCountCacheRepository.exists(pollFeed.feedId())) {
      loadVoteCounts(pollFeed);
    }
  }

  public void increase(FeedSnapshot pollFeed, long optionId) {
    if (!feedVoteCountCacheRepository.incrementIfPresent(pollFeed.feedId(), optionId)) {
      loadVoteCounts(pollFeed);
    }
  }

  private void loadVoteCounts(FeedSnapshot pollFeed) {
    Map<Long, Long> voteCounts = new HashMap<>();
    pollFeed.pollOptionIds().forEach(optionId -> voteCounts.put(optionId, 0L));
    feedVoteRepository
        .countByFeedId(pollFeed.feedId())
        .forEach(voteCount -> voteCounts.put(voteCount.optionId(), voteCount.voteCount()));
    feedVoteCountCacheRepository.saveChangedIfAbsent(pollFeed.feedId(), voteCounts);
  }
}
