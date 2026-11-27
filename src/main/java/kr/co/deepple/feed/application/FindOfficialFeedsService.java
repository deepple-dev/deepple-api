package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedCounts;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.dto.FeedViews;
import kr.co.deepple.feed.application.dto.OfficialFeedGridItemView;
import kr.co.deepple.feed.application.dto.OfficialFeedGridViews;
import kr.co.deepple.feed.application.dto.OfficialFeedSortKey;
import kr.co.deepple.feed.application.dto.PollVotes;
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
  private final FeedSnapshotReader feedSnapshotReader;
  private final LikedFeedReader likedFeedReader;
  private final FeedPollVotesReader feedPollVotesReader;

  @Override
  @Transactional(readOnly = true)
  public OfficialFeedGridViews findOfficialFeeds(long memberId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    if (me.hasNoGrade()) {
      return OfficialFeedGridViews.empty();
    }
    List<OfficialFeed> feeds = findFeeds(me.getGrade(), cursor);
    return toGridViews(feeds);
  }

  @Override
  @Transactional(readOnly = true)
  public FeedViews findOfficialFeedViews(long memberId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    if (me.hasNoGrade()) {
      return FeedViews.empty();
    }
    List<OfficialFeed> feeds = findFeeds(me.getGrade(), cursor);
    if (isLastPage(feeds)) {
      return new FeedViews(toFeedViews(me, feeds), null, false);
    }
    List<OfficialFeed> page = feeds.subList(0, PAGE_SIZE);
    String nextCursor = OfficialFeedSortKey.of(page.getLast()).toCursor();
    return new FeedViews(toFeedViews(me, page), nextCursor, true);
  }

  private List<FeedView> toFeedViews(FeedAuthor me, List<OfficialFeed> feeds) {
    List<FeedSnapshot> snapshots = feeds.stream().map(FeedSnapshot::from).toList();
    Set<Long> feedIds = snapshots.stream().map(FeedSnapshot::feedId).collect(Collectors.toSet());
    Map<Long, FeedCounts> counts = feedCountReader.read(feedIds);
    Set<Long> likedFeedIds = likedFeedReader.findLikedFeedIds(me.getMemberId(), feedIds);
    Map<Long, PollVotes> pollVotes = feedPollVotesReader.read(me, snapshots);
    Map<Long, FeedAuthorSnapshot> authors =
        feedSnapshotReader.readAuthors(
            snapshots.stream().map(FeedSnapshot::authorId).distinct().toList());
    return snapshots.stream()
        .map(feed ->
            feedViewAssembler.toView(
                feed,
                authors.get(feed.authorId()),
                false,
                likedFeedIds.contains(feed.feedId()),
                counts.get(feed.feedId()),
                pollVotes.get(feed.feedId())))
        .toList();
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

  private OfficialFeedGridViews toGridViews(List<OfficialFeed> feeds) {
    if (isLastPage(feeds)) {
      return new OfficialFeedGridViews(toGridItemViews(feeds), null, false);
    }
    List<OfficialFeed> page = feeds.subList(0, PAGE_SIZE);
    String nextCursor = OfficialFeedSortKey.of(page.getLast()).toCursor();
    return new OfficialFeedGridViews(toGridItemViews(page), nextCursor, true);
  }

  private boolean isLastPage(List<OfficialFeed> feeds) {
    return feeds.size() <= PAGE_SIZE;
  }

  private List<OfficialFeedGridItemView> toGridItemViews(List<OfficialFeed> feeds) {
    Map<Long, FeedCounts> counts =
        feedCountReader.read(feeds.stream().map(Feed::getId).collect(Collectors.toSet()));
    return feeds.stream()
        .map(feed ->
            feedViewAssembler.toOfficialGridItemView(
                FeedSnapshot.from(feed), counts.get(feed.getId())))
        .toList();
  }
}
