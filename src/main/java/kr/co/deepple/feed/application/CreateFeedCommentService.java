package kr.co.deepple.feed.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedCommentCreateRequest;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;
import kr.co.deepple.feed.application.port.in.CreateFeedCommentUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateFeedCommentService implements CreateFeedCommentUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedCommentAccessChecker feedCommentAccessChecker;
  private final FeedCommentRepository feedCommentRepository;
  private final FeedCommentCounter feedCommentCounter;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedCommentView createComment(
      long memberId, long feedId, FeedCommentCreateRequest request) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    feedAccessChecker.validateAccessible(me, feedId);
    FeedComment newComment = newComment(me, feedId, request);
    feedCommentCounter.loadIfAbsent(feedId);
    FeedComment comment = feedCommentRepository.save(newComment);
    feedCommentCounter.increase(feedId);
    return feedViewAssembler.toCommentView(new FeedCommentWithAuthor(comment, me), 0L, me);
  }

  private FeedComment newComment(FeedAuthor me, long feedId, FeedCommentCreateRequest request) {
    if (request.parentCommentId() == null) {
      return FeedComment.create(feedId, me.getId(), request.content());
    }
    FeedComment parent = findReplyableParent(me, feedId, request.parentCommentId());
    return FeedComment.reply(parent, me.getId(), request.content());
  }

  private FeedComment findReplyableParent(FeedAuthor me, long feedId, long parentCommentId) {
    FeedComment parent = feedCommentAccessChecker.getVisibleComment(me, parentCommentId);
    if (parent.getFeedId() != feedId) {
      throw new DeeppleException(
          FeedErrorCode.FEED_COMMENT_NOT_FOUND,
          "commentId=" + parentCommentId + ", memberId=" + me.getMemberId());
    }
    return parent;
  }
}
