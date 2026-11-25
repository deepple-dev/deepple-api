package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.AdminFeedCommentView;
import kr.co.deepple.feed.application.port.in.FindAdminFeedCommentUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindAdminFeedCommentService implements FindAdminFeedCommentUseCase {

  private final FeedCommentRepository feedCommentRepository;
  private final FeedAuthorReader feedAuthorReader;

  @Override
  @Transactional(readOnly = true)
  public AdminFeedCommentView getComment(long commentId) {
    FeedComment comment =
        feedCommentRepository
            .findById(commentId)
            .orElseThrow(() ->
                new DeeppleException(
                    FeedErrorCode.FEED_COMMENT_NOT_FOUND, "commentId=" + commentId));
    FeedAuthor author = feedAuthorReader.getAuthor(comment.getAuthorId());
    return new AdminFeedCommentView(
        comment.getId(),
        comment.getFeedId(),
        comment.getParentId(),
        comment.getStatus().name(),
        author.getMemberId(),
        author.getNickname(),
        comment.getContent(),
        comment.getCreatedAt(),
        comment.getDeletedAt());
  }
}
