package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Set;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.feed.application.dto.FeedCommentCursor;
import kr.co.deepple.feed.application.dto.FeedCommentPage;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;
import kr.co.deepple.feed.application.port.in.FindFeedCommentRepliesUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentQueryRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import kr.co.deepple.feed.domain.FeedErrorCode;
import kr.co.deepple.feed.domain.FeedViewer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindFeedCommentRepliesService implements FindFeedCommentRepliesUseCase {

  private static final int PAGE_SIZE = 20;

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedBlockRepository feedBlockRepository;
  private final FeedCommentRepository feedCommentRepository;
  private final FeedCommentQueryRepository feedCommentQueryRepository;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedCommentViews findReplies(long memberId, long commentId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    FeedComment parent = findComment(commentId, me);
    feedAccessChecker.validateAccessible(me, parent.getFeedId());
    Set<Long> blockRelatedMemberIds = feedBlockRepository.findBlockRelatedMemberIds(memberId);
    validateNotBlocked(parent, FeedViewer.of(me, blockRelatedMemberIds), me);
    FeedCommentPage page = findPage(commentId, blockRelatedMemberIds, cursor);
    return new FeedCommentViews(toViews(page, me), page.nextCursor(), page.hasMore());
  }

  private FeedComment findComment(long commentId, FeedAuthor me) {
    return feedCommentRepository
        .findById(commentId)
        .orElseThrow(() -> commentNotFoundException(commentId, me));
  }

  private void validateNotBlocked(FeedComment parent, FeedViewer viewer, FeedAuthor me) {
    if (viewer.isBlockedWith(feedAuthorReader.getAuthor(parent.getAuthorId()))) {
      throw commentNotFoundException(parent.getId(), me);
    }
  }

  private FeedCommentPage findPage(
      long parentCommentId, Set<Long> blockRelatedMemberIds, String cursor) {
    Long lastCommentId =
        FeedCommentCursor.parse(cursor).map(FeedCommentCursor::commentId).orElse(null);
    List<FeedCommentWithAuthor> replies =
        feedCommentQueryRepository.findReplies(
            parentCommentId, blockRelatedMemberIds, lastCommentId, PAGE_SIZE + 1);
    return FeedCommentPage.of(replies, PAGE_SIZE);
  }

  private List<FeedCommentView> toViews(FeedCommentPage page, FeedAuthor me) {
    return page.comments().stream()
        .map(reply -> feedViewAssembler.toCommentView(reply, 0L, me))
        .toList();
  }

  private DeeppleException commentNotFoundException(long commentId, FeedAuthor me) {
    return new DeeppleException(
        FeedErrorCode.FEED_COMMENT_NOT_FOUND,
        "commentId=" + commentId + ", memberId=" + me.getMemberId());
  }
}
