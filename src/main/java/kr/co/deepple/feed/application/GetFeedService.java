package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.in.GetFeedUseCase;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFeedService implements GetFeedUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedCountReader feedCountReader;
  private final LikedFeedReader likedFeedReader;
  private final FeedPollVotesReader feedPollVotesReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedView getFeed(long memberId, long feedId) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    AccessibleFeed accessibleFeed = feedAccessChecker.getAccessibleFeed(me, feedId);
    return feedViewAssembler.toView(
        accessibleFeed.feed(),
        FeedAuthorSnapshot.from(accessibleFeed.author()),
        accessibleFeed.isMine(),
        likedFeedReader.isLiked(memberId, feedId),
        feedCountReader.read(feedId),
        feedPollVotesReader.read(me, accessibleFeed.feed()));
  }
}
