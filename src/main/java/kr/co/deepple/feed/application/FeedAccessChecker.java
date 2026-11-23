package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedRepository;
import kr.co.deepple.feed.domain.Feed;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedViewer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FeedAccessChecker {

  private final FeedRepository feedRepository;
  private final FeedBlockRepository feedBlockRepository;
  private final FeedAuthorReader feedAuthorReader;

  @Transactional(readOnly = true)
  public AccessibleFeed getAccessibleFeed(FeedAuthor me, long feedId) {
    Feed feed = findFeed(feedId, me);
    if (feed.isWrittenBy(me.getId())) {
      return AccessibleFeed.mine(feed, me);
    }
    return AccessibleFeed.others(feed, getAccessibleAuthor(feed, me));
  }

  @Transactional(readOnly = true)
  public void validateAccessible(FeedAuthor me, long feedId) {
    getAccessibleFeed(me, feedId);
  }

  private Feed findFeed(long feedId, FeedAuthor me) {
    return feedRepository
        .findByIdAndDeletedAtIsNull(feedId)
        .orElseThrow(() -> feedNotFoundException(feedId, me));
  }

  private FeedAuthor getAccessibleAuthor(Feed feed, FeedAuthor me) {
    if (!feed.isVisibleTo(me.getGrade())) {
      throw feedNotFoundException(feed.getId(), me);
    }
    FeedAuthor author = feedAuthorReader.getAuthor(feed.getAuthorId());
    if (!createViewer(me).canSeeAuthor(author)) {
      throw feedNotFoundException(feed.getId(), me);
    }
    return author;
  }

  private FeedViewer createViewer(FeedAuthor me) {
    return FeedViewer.of(me, feedBlockRepository.findBlockRelatedMemberIds(me.getMemberId()));
  }

  private DeeppleException feedNotFoundException(long feedId, FeedAuthor me) {
    return new DeeppleException(
        FeedErrorCode.FEED_NOT_FOUND, "feedId=" + feedId + ", memberId=" + me.getMemberId());
  }
}
