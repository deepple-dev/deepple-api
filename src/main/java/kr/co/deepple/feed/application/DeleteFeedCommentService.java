package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.port.in.DeleteFeedCommentUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFeedCommentService implements DeleteFeedCommentUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedCommentRepository feedCommentRepository;

  @Override
  @Transactional
  public void deleteComment(long memberId, long commentId) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    FeedComment comment =
        feedCommentRepository
            .findByIdAndAuthorId(commentId, me.getId())
            .orElseThrow(() -> commentNotFoundException(commentId, memberId));
    comment.delete();
  }

  private DeeppleException commentNotFoundException(long commentId, long memberId) {
    return new DeeppleException(
        FeedErrorCode.FEED_COMMENT_NOT_FOUND, "commentId=" + commentId + ", memberId=" + memberId);
  }
}
