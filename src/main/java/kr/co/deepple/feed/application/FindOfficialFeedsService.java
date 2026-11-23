package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedGridItemView;
import kr.co.deepple.feed.application.dto.FeedGridViews;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.OfficialFeedSortKey;
import kr.co.deepple.feed.application.port.in.FindOfficialFeedsUseCase;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.OfficialFeed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindOfficialFeedsService implements FindOfficialFeedsUseCase {

  private static final int PAGE_SIZE = 20;
  private static final Limit FETCH_LIMIT = Limit.of(PAGE_SIZE + 1);

  private final FeedRepository feedRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final FeedCountReader feedCountReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  @Transactional(readOnly = true)
  public FeedGridViews findOfficialFeeds(long memberId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    if (me.hasNoGrade()) {
      return FeedGridViews.empty();
    }
    List<OfficialFeed> feeds = findFeeds(me.getGrade(), cursor);
    return toGridViews(feeds);
  }

  private List<OfficialFeed> findFeeds(String grade, String cursor) {
    Optional<OfficialFeedSortKey> after = OfficialFeedSortKey.parse(cursor);
    if (after.isEmpty()) {
      return feedRepository.findFirstOfficialFeeds(grade, FETCH_LIMIT);
    }
    return findFeedsAfter(grade, after.get());
  }

  private List<OfficialFeed> findFeedsAfter(String grade, OfficialFeedSortKey after) {
    return feedRepository.findOfficialFeedsAfter(
        grade, after.isPinned(), after.publishedAt(), after.feedId(), FETCH_LIMIT);
  }

  private FeedGridViews toGridViews(List<OfficialFeed> feeds) {
    if (isLastPage(feeds)) {
      return new FeedGridViews(toGridItemViews(feeds), null, false);
    }
    List<OfficialFeed> page = feeds.subList(0, PAGE_SIZE);
    String nextCursor = OfficialFeedSortKey.of(page.getLast()).toCursor();
    return new FeedGridViews(toGridItemViews(page), nextCursor, true);
  }

  private boolean isLastPage(List<OfficialFeed> feeds) {
    return feeds.size() <= PAGE_SIZE;
  }

  private List<FeedGridItemView> toGridItemViews(List<OfficialFeed> feeds) {
    Map<Long, FeedCounts> counts =
        feedCountReader.read(feeds.stream().map(Feed::getId).collect(Collectors.toSet()));
    return feeds.stream()
        .map(feed ->
            feedViewAssembler.toGridItemView(FeedSnapshot.from(feed), counts.get(feed.getId())))
        .toList();
  }
}
