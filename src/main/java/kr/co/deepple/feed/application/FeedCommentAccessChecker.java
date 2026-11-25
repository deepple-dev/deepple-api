package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedViewer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FeedCommentAccessChecker {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedBlockRepository feedBlockRepository;
  private final FeedCommentRepository feedCommentRepository;

  @Transactional(readOnly = true)
  public FeedComment getVisibleComment(FeedAuthor me, long commentId) {
    FeedComment comment =
        feedCommentRepository
            .findById(commentId)
            .orElseThrow(() -> commentNotFoundException(commentId, me));
    if (!isVisible(comment, me)) {
      throw commentNotFoundException(commentId, me);
    }
    return comment;
  }

  private boolean isVisible(FeedComment comment, FeedAuthor me) {
    if (comment.isDeleted()) {
      return false;
    }
    if (comment.isHiddenByReport()) {
      return false;
    }
    return canSeeAuthor(comment, me);
  }

  private boolean canSeeAuthor(FeedComment comment, FeedAuthor me) {
    FeedViewer viewer =
        FeedViewer.of(me, feedBlockRepository.findBlockRelatedMemberIds(me.getMemberId()));
    return viewer.canSeeAuthor(feedAuthorReader.getAuthor(comment.getAuthorId()));
  }

  private DeeppleException commentNotFoundException(long commentId, FeedAuthor me) {
    return new DeeppleException(
        FeedErrorCode.FEED_COMMENT_NOT_FOUND,
        "commentId=" + commentId + ", memberId=" + me.getMemberId());
  }
}
