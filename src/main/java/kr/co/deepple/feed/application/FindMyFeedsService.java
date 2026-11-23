package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.port.in.FindMyFeedsUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindMyFeedsService implements FindMyFeedsUseCase {

  private static final int PAGE_SIZE = 20;

  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final FeedCountReader feedCountReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  @Transactional(readOnly = true)
  public FeedGridViews findMyFeeds(long memberId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    List<Feed> feeds = findFeeds(me.getId(), parseCursor(cursor));
    boolean hasMore = feeds.size() > PAGE_SIZE;
    List<Feed> page = hasMore ? feeds.subList(0, PAGE_SIZE) : feeds;
    String nextCursor = hasMore ? String.valueOf(page.getLast().getId()) : null;
    return new FeedGridViews(toGridItemViews(page), nextCursor, hasMore);
  }

  private List<Feed> findFeeds(long authorId, Long lastFeedId) {
    Limit limit = Limit.of(PAGE_SIZE + 1);
    if (lastFeedId == null) {
      return feedRepository.findByAuthorIdAndDeletedAtIsNullOrderByIdDesc(authorId, limit);
    }
    return feedRepository.findByAuthorIdAndIdLessThanAndDeletedAtIsNullOrderByIdDesc(
        authorId, lastFeedId, limit);
  }

  private List<FeedGridItemView> toGridItemViews(List<Feed> feeds) {
    Map<Long, FeedCounts> counts =
        feedCountReader.read(feeds.stream().map(Feed::getId).collect(Collectors.toSet()));
    return feeds.stream()
        .map(feed ->
            feedViewAssembler.toGridItemView(FeedSnapshot.from(feed), counts.get(feed.getId())))
        .toList();
  }

  private Long parseCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(cursor);
    } catch (NumberFormatException e) {
      throw new DeeppleException(FeedErrorCode.INVALID_FEED_CURSOR, "cursor=" + cursor);
    }
  }
}
