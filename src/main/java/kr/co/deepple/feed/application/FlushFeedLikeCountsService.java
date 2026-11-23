package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.port.in.FlushFeedLikeCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlushFeedLikeCountsService implements FlushFeedLikeCountsUseCase {

  private static final int BATCH_SIZE = 500;

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;
  private final FeedCountRepository feedCountRepository;

  @Override
  public void flush() {
    boolean hasMore = true;
    while (hasMore) {
      hasMore = flushNextBatch();
    }
  }

  private boolean flushNextBatch() {
    List<Long> feedIds = feedLikeCountCacheRepository.popChangedFeedIds(BATCH_SIZE);
    Map<Long, Long> likeCounts = feedLikeCountCacheRepository.findAll(feedIds);
    upsertLikeCounts(likeCounts);
    return feedIds.size() == BATCH_SIZE;
  }

  private void upsertLikeCounts(Map<Long, Long> likeCounts) {
    LocalDateTime now = LocalDateTime.now();
    likeCounts.forEach(
        (feedId, likeCount) -> feedCountRepository.upsertLikeCount(feedId, likeCount, now));
  }
}
