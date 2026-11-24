package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.port.in.FlushFeedVoteCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlushFeedVoteCountsService implements FlushFeedVoteCountsUseCase {

  private static final int BATCH_SIZE = 500;

  private final FeedVoteCountCacheRepository feedVoteCountCacheRepository;
  private final FeedVoteCountRepository feedVoteCountRepository;

  @Override
  public void flush() {
    boolean hasMore = true;
    while (hasMore) {
      hasMore = flushNextBatch();
    }
  }

  private boolean flushNextBatch() {
    List<Long> feedIds = feedVoteCountCacheRepository.popChangedFeedIds(BATCH_SIZE);
    Map<Long, Map<Long, Long>> voteCounts = feedVoteCountCacheRepository.findAll(feedIds);
    upsertVoteCounts(voteCounts);
    return feedIds.size() == BATCH_SIZE;
  }

  private void upsertVoteCounts(Map<Long, Map<Long, Long>> voteCounts) {
    LocalDateTime now = LocalDateTime.now();
    voteCounts.forEach((feedId, counts) ->
        counts.forEach((optionId, voteCount) ->
            feedVoteCountRepository.upsertVoteCount(optionId, feedId, voteCount, now)));
  }
}
