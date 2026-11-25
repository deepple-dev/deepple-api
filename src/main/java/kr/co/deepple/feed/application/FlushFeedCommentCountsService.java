package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.port.in.FlushFeedCommentCountsUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlushFeedCommentCountsService implements FlushFeedCommentCountsUseCase {

  private static final int BATCH_SIZE = 500;

  private final FeedCommentCountCacheRepository feedCommentCountCacheRepository;
  private final FeedCountRepository feedCountRepository;

  @Override
  public void flush() {
    boolean hasMore = true;
    while (hasMore) {
      hasMore = flushNextBatch();
    }
  }

  private boolean flushNextBatch() {
    List<Long> feedIds = feedCommentCountCacheRepository.popChangedFeedIds(BATCH_SIZE);
    Map<Long, Long> commentCounts = feedCommentCountCacheRepository.findAll(feedIds);
    upsertCommentCounts(commentCounts);
    return feedIds.size() == BATCH_SIZE;
  }

  private void upsertCommentCounts(Map<Long, Long> commentCounts) {
    LocalDateTime now = LocalDateTime.now();
    commentCounts.forEach((feedId, commentCount) ->
        feedCountRepository.upsertCommentCount(feedId, commentCount, now));
  }
}
