package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.in.HideReportedContentUseCase;
import kr.co.deepple.feed.application.port.out.FeedCommentRepository;
import kr.co.deepple.feed.domain.FeedComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HideReportedContentService implements HideReportedContentUseCase {

  private final FeedWriter feedWriter;
  private final FeedCommentRepository feedCommentRepository;

  @Override
  public void hideFeed(long feedId) {
    feedWriter.hideUserFeedByReport(feedId);
  }

  @Override
  @Transactional
  public void hideComment(long commentId) {
    feedCommentRepository
        .findByIdForUpdate(commentId)
        .filter(comment -> !comment.isHiddenByReport())
        .ifPresent(this::hide);
  }

  private void hide(FeedComment comment) {
    comment.hideByReport();
    log.atInfo().addKeyValue("commentId", comment.getId()).log("신고 누적으로 댓글 숨김");
  }
}
