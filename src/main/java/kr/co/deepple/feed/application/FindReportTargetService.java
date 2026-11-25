package kr.co.deepple.feed.application;

import java.util.Optional;
import kr.co.deepple.feed.application.port.in.FindReportTargetUseCase;
import kr.co.deepple.feed.domain.FeedAuthor;
import kr.co.deepple.feed.domain.FeedComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindReportTargetService implements FindReportTargetUseCase {

  private final FeedAuthorReader feedAuthorReader;
  private final FeedAccessChecker feedAccessChecker;
  private final FeedCommentAccessChecker feedCommentAccessChecker;

  @Override
  @Transactional(readOnly = true)
  public Optional<Long> findFeedAuthorMemberId(long reporterId, long feedId) {
    FeedAuthor me = feedAuthorReader.getMember(reporterId);
    AccessibleFeed feed = feedAccessChecker.getAccessibleFeed(me, feedId);
    return Optional.ofNullable(feed.author().getMemberId());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Long> findCommentAuthorMemberId(long reporterId, long commentId) {
    FeedAuthor me = feedAuthorReader.getMember(reporterId);
    FeedComment comment = feedCommentAccessChecker.getVisibleComment(me, commentId);
    feedAccessChecker.validateAccessible(me, comment.getFeedId());
    return Optional.ofNullable(feedAuthorReader.getAuthor(comment.getAuthorId()).getMemberId());
  }
}
