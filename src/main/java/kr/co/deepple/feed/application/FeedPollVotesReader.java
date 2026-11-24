package kr.co.deepple.feed.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.PollVotes;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedPollVotesReader {

  private final VotedFeedReader votedFeedReader;
  private final FeedVoteCountReader feedVoteCountReader;

  public Map<Long, PollVotes> read(FeedAuthor me, Collection<FeedSnapshot> feeds) {
    List<FeedSnapshot> pollFeeds = feeds.stream().filter(FeedSnapshot::isPoll).toList();
    Map<Long, Long> myOptionIds =
        votedFeedReader.findVotedOptionIds(me.getMemberId(), toFeedIds(pollFeeds));
    List<FeedSnapshot> resultVisibleFeeds =
        pollFeeds.stream()
            .filter(feed -> isResultVisible(me, feed, myOptionIds.get(feed.feedId())))
            .toList();
    Map<Long, Map<Long, Long>> voteCounts = feedVoteCountReader.read(resultVisibleFeeds);
    return feeds.stream()
        .collect(
            Collectors.toMap(
                FeedSnapshot::feedId,
                feed -> toPollVotes(me, feed, myOptionIds.get(feed.feedId()), voteCounts)));
  }

  public PollVotes read(FeedAuthor me, FeedSnapshot feed) {
    return read(me, List.of(feed)).get(feed.feedId());
  }

  private Set<Long> toFeedIds(List<FeedSnapshot> feeds) {
    return feeds.stream().map(FeedSnapshot::feedId).collect(Collectors.toSet());
  }

  private boolean isResultVisible(FeedAuthor me, FeedSnapshot feed, Long myOptionId) {
    return feed.isWrittenBy(me.getId()) || myOptionId != null;
  }

  private PollVotes toPollVotes(
      FeedAuthor me, FeedSnapshot feed, Long myOptionId, Map<Long, Map<Long, Long>> voteCounts) {
    if (!feed.isPoll()) {
      return PollVotes.hidden();
    }
    if (!isResultVisible(me, feed, myOptionId)) {
      return PollVotes.hidden();
    }
    return PollVotes.visible(myOptionId, voteCounts.get(feed.feedId()));
  }
}
