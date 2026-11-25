package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.co.deepple.feed.application.dto.FeedCommentCursor;
import kr.co.deepple.feed.application.dto.FeedCommentPage;
import kr.co.deepple.feed.application.dto.FeedCommentView;
import kr.co.deepple.feed.application.dto.FeedCommentViews;
import kr.co.deepple.feed.application.dto.FeedCommentWithAuthor;
import kr.co.deepple.feed.application.port.in.FindFeedCommentsUseCase;
import kr.co.deepple.feed.application.port.out.FeedBlockRepository;
import kr.co.deepple.feed.application.port.out.FeedCommentQueryRepository;
import kr.co.deepple.feed.domain.FeedAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindFeedCommentsService implements FindFeedCommentsUseCase {

  private static final int PAGE_SIZE = 20;

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedBlockRepository feedBlockRepository;
  private final FeedCommentQueryRepository feedCommentQueryRepository;
  private final FeedViewAssembler feedViewAssembler;

  @Override
  public FeedCommentViews findComments(long memberId, long feedId, String cursor) {
    FeedAuthor me = feedAuthorReader.getMember(memberId);
    feedAccessChecker.validateAccessible(me, feedId);
    Set<Long> blockRelatedMemberIds = feedBlockRepository.findBlockRelatedMemberIds(memberId);
    FeedCommentPage page = findPage(feedId, blockRelatedMemberIds, cursor);
    Map<Long, Long> replyCounts =
        feedCommentQueryRepository.countReplies(page.commentIds(), blockRelatedMemberIds);
    return new FeedCommentViews(toViews(page, replyCounts, me), page.nextCursor(), page.hasMore());
  }

  private FeedCommentPage findPage(long feedId, Set<Long> blockRelatedMemberIds, String cursor) {
    Long lastCommentId =
        FeedCommentCursor.parse(cursor).map(FeedCommentCursor::commentId).orElse(null);
    List<FeedCommentWithAuthor> comments =
        feedCommentQueryRepository.findTopLevelComments(
            feedId, blockRelatedMemberIds, lastCommentId, PAGE_SIZE + 1);
    return FeedCommentPage.of(comments, PAGE_SIZE);
  }

  private List<FeedCommentView> toViews(
      FeedCommentPage page, Map<Long, Long> replyCounts, FeedAuthor me) {
    return page.comments().stream()
        .map(comment ->
            feedViewAssembler.toCommentView(
                comment, replyCounts.getOrDefault(comment.comment().getId(), 0L), me))
        .toList();
  }
}
