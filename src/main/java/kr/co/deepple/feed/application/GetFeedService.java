package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.in.GetFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetFeedService implements GetFeedUseCase {

  private final FeedRepository feedRepository;
  private final FeedBlockRepository feedBlockRepository;
  private final FeedAuthorReader feedAuthorReader;
  private final FeedCountReader feedCountReader;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  @Transactional(readOnly = true)
  public FeedView getFeed(long memberId, long feedId) {
    FeedAuthor viewer = feedAuthorReader.getMember(memberId);
    Feed feed =
        feedRepository
            .findByIdAndDeletedAtIsNull(feedId)
            .orElseThrow(() -> feedNotFoundException(feedId, memberId));
    boolean isMine = feed.isWrittenBy(viewer.getId());
    if (isMine) {
      return feedViewAssembler.toView(feed, viewer, isMine, feedCountReader.read(feedId));
    }
    FeedAuthor author = validateVisibleToOther(feed, viewer);
    return feedViewAssembler.toView(feed, author, isMine, feedCountReader.read(feedId));
  }

  private FeedAuthor validateVisibleToOther(Feed feed, FeedAuthor viewer) {
    if (!feed.isVisibleTo(viewer.getGrade())) {
      throw feedNotFoundException(feed.getId(), viewer.getMemberId());
    }
    FeedAuthor author = feedAuthorReader.getAuthor(feed.getAuthorId());
    if (author.isOfficial()) {
      return author;
    }
    if (!author.isActive()) {
      throw feedNotFoundException(feed.getId(), viewer.getMemberId());
    }
    if (feedBlockRepository.existsBetween(viewer.getMemberId(), author.getMemberId())) {
      throw feedNotFoundException(feed.getId(), viewer.getMemberId());
    }
    return author;
  }

  private DeeppleException feedNotFoundException(long feedId, long memberId) {
    return new DeeppleException(
        FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId + ", memberId=" + memberId);
  }
}
