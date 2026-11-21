package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedAuthorSnapshot;
import kr.co.deepple.feed.application.dto.FeedSnapshot;
import kr.co.deepple.feed.application.dto.FeedView;
import kr.co.deepple.feed.application.port.in.GetFeedUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedViewer;
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
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    Feed feed = findFeed(feedId, memberId);
    if (feed.isWrittenBy(me.getId())) {
      return toMyFeedView(feed, me);
    }
    FeedAuthor author = getVisibleAuthor(feed, me);
    return toOtherFeedView(feed, author);
  }

  private Feed findFeed(long feedId, long memberId) {
    return feedRepository
        .findByIdAndDeletedAtIsNull(feedId)
        .orElseThrow(() -> feedNotFoundException(feedId, memberId));
  }

  private FeedView toMyFeedView(Feed feed, FeedAuthor me) {
    return toView(feed, me, true);
  }

  private FeedAuthor getVisibleAuthor(Feed feed, FeedAuthor me) {
    if (!feed.isVisibleTo(me.getGrade())) {
      throw feedNotFoundException(feed.getId(), me.getMemberId());
    }
    FeedAuthor author = feedAuthorReader.getAuthor(feed.getAuthorId());
    if (!createViewer(me).canSeeAuthor(author)) {
      throw feedNotFoundException(feed.getId(), me.getMemberId());
    }
    return author;
  }

  private FeedViewer createViewer(FeedAuthor me) {
    return FeedViewer.of(me, feedBlockRepository.findBlockRelatedMemberIds(me.getMemberId()));
  }

  private FeedView toOtherFeedView(Feed feed, FeedAuthor author) {
    return toView(feed, author, false);
  }

  private FeedView toView(Feed feed, FeedAuthor author, boolean isMine) {
    return feedViewAssembler.toView(
        FeedSnapshot.from(feed),
        FeedAuthorSnapshot.from(author),
        isMine,
        feedCountReader.read(feed.getId()));
  }

  private DeeppleException feedNotFoundException(long feedId, long memberId) {
    return new DeeppleException(
        FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId + ", memberId=" + memberId);
  }
}
