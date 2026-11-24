package kr.co.deepple.feed.application;

import java.time.LocalDateTime;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedPollView;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.PollVotes;
import kr.co.deepple.feed.application.port.in.VoteFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedVoteRepository;
import kr.co.deepple.feed.application.port.out.VotedFeedCacheRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteFeedService implements VoteFeedUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedVoteRepository feedVoteRepository;
  private final FeedVoteCounter feedVoteCounter;
  private final VotedFeedCacheRepository votedFeedCacheRepository;
  private final FeedVoteCountReader feedVoteCountReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedPollView vote(long memberId, long feedId, long optionId) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    AccessibleFeed accessibleFeed = feedAccessChecker.getAccessibleFeed(me, feedId);
    validateNotMine(accessibleFeed, memberId);
    FeedSnapshot feed = accessibleFeed.feed();
    validatePollOption(feed, optionId);
    saveVote(memberId, feed, optionId);
    increaseCachedVote(memberId, feed, optionId);
    return feedViewAssembler.toPollView(
        feed, PollVotes.visible(optionId, feedVoteCountReader.read(feed)));
  }

  private void validateNotMine(AccessibleFeed accessibleFeed, long memberId) {
    if (accessibleFeed.isMine()) {
      throw selfFeedVoteException(accessibleFeed.feed().feedId(), memberId);
    }
  }

  private void validatePollOption(FeedSnapshot feed, long optionId) {
    if (!feed.hasPollOption(optionId)) {
      throw pollOptionNotFoundException(feed, optionId);
    }
  }

  private void saveVote(long memberId, FeedSnapshot feed, long optionId) {
    int savedCount =
        feedVoteRepository.insertIfAbsent(feed.feedId(), memberId, optionId, LocalDateTime.now());
    if (savedCount == 0) {
      throw voteAlreadyExistsException(memberId, feed.feedId());
    }
  }

  private void increaseCachedVote(long memberId, FeedSnapshot feed, long optionId) {
    feedVoteCounter.increase(feed, optionId);
    votedFeedCacheRepository.addIfPresent(memberId, feed.feedId(), optionId);
  }

  private DeeppleException selfFeedVoteException(long feedId, long memberId) {
    return new DeeppleException(
        FeedErrorCode.SELF_FEED_VOTE, "feedId=" + feedId + ", memberId=" + memberId);
  }

  private DeeppleException pollOptionNotFoundException(FeedSnapshot feed, long optionId) {
    return new DeeppleException(
        FeedErrorCode.POLL_OPTION_NOT_FOUND,
        "feedId=" + feed.feedId() + ", type=" + feed.type() + ", optionId=" + optionId);
  }

  private DeeppleException voteAlreadyExistsException(long memberId, long feedId) {
    return new DeeppleException(
        FeedErrorCode.FEED_VOTE_ALREADY_EXISTS, "feedId=" + feedId + ", memberId=" + memberId);
  }
}
