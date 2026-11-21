package kr.co.deepple.feed.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.HomeFeedSortKey;
import kr.co.deepple.feed.application.port.in.FindHomeFeedsUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.HomeFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedViewer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindHomeFeedsService implements FindHomeFeedsUseCase {

  private static final int PAGE_SIZE = 20;
  private static final int FETCH_SIZE = PAGE_SIZE + 1;

  private final FeedAuthorReader feedAuthorReader;
  private final FeedBlockRepository feedBlockRepository;
  private final HomeFeedListLoader homeFeedListLoader;
  private final HomeFeedCacheRepository homeFeedCacheRepository;
  private final FeedSnapshotReader feedSnapshotReader;
  private final FeedCountReader feedCountReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedViews findHomeFeeds(long memberId, String cursor) {
    HomeFeedSortKey after = HomeFeedSortKey.parse(cursor).orElse(HomeFeedSortKey.START);
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    if (me.hasNoGrade()) {
      return FeedViews.empty();
    }
    homeFeedListLoader.loadIfNotLoaded(me.getGrade());
    Page page = findPage(createViewer(me), after);
    return toViews(page);
  }

  private FeedViewer createViewer(FeedAuthor me) {
    return FeedViewer.of(me, feedBlockRepository.findBlockRelatedMemberIds(me.getMemberId()));
  }

  private Page findPage(FeedViewer viewer, HomeFeedSortKey after) {
    List<HomeFeedSortKey> candidates =
        homeFeedCacheRepository.findSortKeys(viewer.grade(), after.score(), FETCH_SIZE);
    List<HomeFeedSortKey> pageSortKeys = selectPageSortKeys(candidates, after);
    List<FeedWithAuthor> shownFeeds =
        findFeedsWithAuthor(pageSortKeys).stream()
            .filter(feedWithAuthor -> isShownOnHome(viewer, feedWithAuthor))
            .toList();
    if (isLastPage(candidates, pageSortKeys)) {
      return Page.last(shownFeeds);
    }
    return new Page(shownFeeds, pageSortKeys.getLast());
  }

  private List<HomeFeedSortKey> selectPageSortKeys(
      List<HomeFeedSortKey> candidates, HomeFeedSortKey after) {
    return candidates.stream().filter(sortKey -> sortKey.isAfter(after)).limit(PAGE_SIZE).toList();
  }

  private List<FeedWithAuthor> findFeedsWithAuthor(List<HomeFeedSortKey> sortKeys) {
    Map<Long, FeedSnapshot> feeds =
        feedSnapshotReader.readPublishedFeeds(
            sortKeys.stream().map(HomeFeedSortKey::feedId).toList());
    Map<Long, FeedAuthorSnapshot> authors =
        feedSnapshotReader.readAuthors(
            feeds.values().stream().map(FeedSnapshot::authorId).distinct().toList());
    List<FeedWithAuthor> feedsWithAuthor = new ArrayList<>();
    for (HomeFeedSortKey sortKey : sortKeys) {
      FeedSnapshot feed = feeds.get(sortKey.feedId());
      if (feed == null) {
        continue;
      }
      FeedAuthorSnapshot author = authors.get(feed.authorId());
      if (author == null) {
        continue;
      }
      feedsWithAuthor.add(new FeedWithAuthor(feed, author));
    }
    return feedsWithAuthor;
  }

  private boolean isShownOnHome(FeedViewer viewer, FeedWithAuthor feedWithAuthor) {
    if (feedWithAuthor.feed().isWrittenBy(viewer.authorId())) {
      return false;
    }
    return viewer.canSeeAuthor(feedWithAuthor.author());
  }

  private boolean isLastPage(List<HomeFeedSortKey> candidates, List<HomeFeedSortKey> pageSortKeys) {
    return candidates.size() < FETCH_SIZE || pageSortKeys.isEmpty();
  }

  private FeedViews toViews(Page page) {
    List<FeedView> views = toFeedViews(page.feedsWithAuthor());
    if (page.isLast()) {
      return new FeedViews(views, null, false);
    }
    return new FeedViews(views, page.next().toCursor(), true);
  }

  private List<FeedView> toFeedViews(List<FeedWithAuthor> feedsWithAuthor) {
    Map<Long, FeedCounts> counts =
        feedCountReader.read(
            feedsWithAuthor.stream()
                .map(feedWithAuthor -> feedWithAuthor.feed().feedId())
                .toList());
    return feedsWithAuthor.stream()
        .map(feedWithAuthor ->
            feedViewAssembler.toView(
                feedWithAuthor.feed(),
                feedWithAuthor.author(),
                false,
                counts.get(feedWithAuthor.feed().feedId())))
        .toList();
  }

  private record FeedWithAuthor(FeedSnapshot feed, FeedAuthorSnapshot author) {}

  private record Page(List<FeedWithAuthor> feedsWithAuthor, HomeFeedSortKey next) {

    static Page last(List<FeedWithAuthor> feedsWithAuthor) {
      return new Page(feedsWithAuthor, null);
    }

    boolean isLast() {
      return next == null;
    }
  }
}
